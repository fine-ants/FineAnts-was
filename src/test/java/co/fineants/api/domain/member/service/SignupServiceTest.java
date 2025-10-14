package co.fineants.api.domain.member.service;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;

import co.fineants.TestDataFactory;
import co.fineants.api.global.errors.exception.business.EmailDuplicateException;
import co.fineants.api.global.errors.exception.business.MemberProfileUploadException;
import co.fineants.api.global.errors.exception.business.NicknameDuplicateException;
import co.fineants.member.application.MemberProfileFactory;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberEmail;
import co.fineants.member.domain.MemberProfile;
import co.fineants.member.domain.Nickname;
import co.fineants.member.domain.NotificationPreference;
import co.fineants.member.infrastructure.MemberRepository;
import co.fineants.member.presentation.dto.request.SignUpRequest;

class SignupServiceTest extends co.fineants.AbstractContainerBaseTest {

	@Autowired
	private SignupService service;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private AmazonS3 amazonS3;

	@Value("${aws.s3.bucket}")
	private String bucketName;

	@Value("${aws.s3.profile-path}")
	private String profilePath;

	@Autowired
	private MemberProfileFactory profileFactory;

	private static Stream<Arguments> invalidProfileFileSource() {
		MultipartFile emptyFile = new MockMultipartFile("file", "", "text/plain", new byte[0]); // 빈 파일
		return Stream.of(
			Arguments.of((Object)null), // null 파일
			Arguments.of(emptyFile)
		);
	}

	private static Stream<Arguments> invalidProfileUrlSource() {
		return Stream.of(
			Arguments.of((String)null), // null URL
			Arguments.of(""), // 빈 문자열
			Arguments.of("invalidUrl"), // 잘못된 형식의 URL
			Arguments.of("https://example.com/invalid/path/profile.jpeg"), // S3 경로가 아닌 URL
			Arguments.of("https://fineants.s3.ap-northeast-2.amazonaws.com/invalid/path/profile.jpeg")
			// S3 경로가 맞지만 잘못된 키
		);
	}

	@DisplayName("사용자는 회원가입시 회원 정보를 저장한다")
	@Test
	void should_saveMember_whenSignup() {
		// given
		MemberEmail memberEmail = new MemberEmail("ants1@gmail.com");
		Nickname nickname = new Nickname("ants1");
		MemberProfile profile = MemberProfile.localMemberProfile(memberEmail, nickname, "ants1234@", null);
		NotificationPreference notificationPreference = NotificationPreference.defaultSetting();
		Member member = Member.createMember(profile, notificationPreference);

		// when
		service.signup(member);
		// then
		int memberSize = memberRepository.findAll().size();
		assertThat(memberSize).isEqualTo(1);
	}

	@DisplayName("사용자는 이미 존재하는 닉네임을 가지고 회원가입 할 수 없다.")
	@Test
	void givenDuplicatedNickname_whenValidateNickname_thenFailSignup() {
		// given
		MemberEmail memberEmail = new MemberEmail("ants1234@gmail.com");
		Nickname nickname = new Nickname("ants1234");
		MemberProfile profile = MemberProfile.localMemberProfile(memberEmail, nickname, "ants1234@", null);
		NotificationPreference notificationPreference = NotificationPreference.defaultSetting();
		Member member = Member.createMember(profile, notificationPreference);
		memberRepository.save(member);

		MemberEmail changeMemberEmail = new MemberEmail("ants4567@gmail.com");
		MemberProfile otherProfile = MemberProfile.localMemberProfile(changeMemberEmail, nickname, "ants4567@",
			null);

		Member otherMember = Member.createMember(otherProfile, notificationPreference);
		// when
		Throwable throwable = catchThrowable(() -> service.signup(otherMember));
		// then
		assertThat(throwable)
			.isInstanceOf(NicknameDuplicateException.class);
	}

	@DisplayName("파일이 없는 경우에는 비어있는 Optional을 반환한다")
	@ParameterizedTest
	@MethodSource(value = "invalidProfileFileSource")
	void givenEmptyFile_whenUpload_thenReturnEmptyOfOptional(MultipartFile file) {
		// given
		// when
		Optional<String> profileUrl = service.upload(file);
		// then
		assertThat(profileUrl).isEmpty();
	}

	@DisplayName("업로드된 파일의 URL이 주어지고 프로필 사진을 제거하면 S3에 해당 파일이 삭제된다")
	@Test
	void should_deleteProfileImage_whenUploadAndDelete() {
		// given
		MultipartFile profileFile = createProfileFile();
		String profileUrl = service.upload(profileFile).orElseThrow();
		String key = extractKeyFromUrl(profileUrl);
		assertThat(amazonS3.doesObjectExist(bucketName, key)).isTrue();
		// when
		service.deleteProfileImageFile(profileUrl);
		// then
		assertThat(amazonS3.doesObjectExist(bucketName, key)).isFalse();
	}

	private static MultipartFile createProfileFile() {
		ClassPathResource classPathResource = new ClassPathResource("profile.jpeg");
		try {
			Path path = Paths.get(classPathResource.getURI());
			byte[] profile = Files.readAllBytes(path);
			return new MockMultipartFile("profileImageFile", "profile.jpeg", "image/jpeg",
				profile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String extractKeyFromUrl(String url) {
		Pattern pattern = Pattern.compile(profilePath + "[0-9a-f\\-]+profile\\.jpeg");
		Matcher matcher = pattern.matcher(url);
		if (matcher.find()) {
			return matcher.group();
		}
		throw new IllegalArgumentException("Invalid URL: " + url);
	}

	@DisplayName("유효하지 않은 프로필 URL이 주어지고 삭제하려고 할때 예외가 발생하지 않는다")
	@ParameterizedTest
	@MethodSource(value = "invalidProfileUrlSource")
	void givenInvalidProfileUrl_whenDelete_thenNotThrowException(String profileUrl) {
		// when & then
		assertThatCode(() -> {
			// 테스트 대상 코드
			service.deleteProfileImageFile(profileUrl);
		}).doesNotThrowAnyException();
	}

	@DisplayName("사용자는 일반 회원가입한다")
	@Test
	void signup() {
		// given
		SignUpRequest request = new SignUpRequest(
			"일개미1234",
			"ants1234@gmail.com",
			"ants1234@",
			"ants1234@"
		);
		MultipartFile profileImageFile = createProfileFile();
		String profileUrl = service.upload(profileImageFile).orElse(null);
		MemberProfile profile = profileFactory.localMemberProfile(request.getEmail(), request.getNickname(),
			request.getPassword(), profileUrl);
		NotificationPreference notificationPreference = NotificationPreference.defaultSetting();
		Member member = Member.createMember(profile, notificationPreference);

		// when
		service.signup(member);

		// then
		Assertions.assertThat(memberRepository.findAll())
			.hasSize(1)
			.contains(member);
	}

	@DisplayName("사용자는 일반 회원가입 할때 프로필 사진을 기본 프로필 사진으로 가입한다")
	@Test
	void signup_whenDefaultProfile_thenSaveDefaultProfileUrl() {
		// given
		SignUpRequest request = new SignUpRequest(
			"일개미1234",
			"ants1234@gmail.com",
			"ants1234@",
			"ants1234@"
		);
		String profileUrl = null;
		MemberProfile profile = profileFactory.localMemberProfile(request.getEmail(), request.getNickname(),
			request.getPassword(), profileUrl);
		NotificationPreference notificationPreference = NotificationPreference.defaultSetting();
		Member member = Member.createMember(profile, notificationPreference);
		// when
		service.signup(member);

		// then
		Member findMember = memberRepository.findAll().stream().findAny().orElseThrow();
		Assertions.assertThat(findMember).isNotNull();
		Assertions.assertThat(findMember.getProfileUrl()).isEmpty();
	}

	@DisplayName("사용자는 닉네임이 중복되어 회원가입 할 수 없다")
	@TestFactory
	Stream<DynamicTest> duplicateNickname() {
		final String nickname = "일개미1234";
		return Stream.of(
			DynamicTest.dynamicTest("회원가입을 정상 진행한다", () -> {
				// given
				SignUpRequest request = new SignUpRequest(
					nickname,
					"ants1234@gmail.com",
					"ants1234@",
					"ants1234@"
				);

				MemberProfile profile = profileFactory.localMemberProfile(request.getEmail(), request.getNickname(),
					request.getPassword(), null);
				NotificationPreference notificationPreference = NotificationPreference.defaultSetting();
				Member member = Member.createMember(profile, notificationPreference);

				// when
				service.signup(member);
				// then
				Member findMember = memberRepository.findAll().stream().findAny().orElseThrow();
				Assertions.assertThat(findMember).isNotNull();
			}),
			DynamicTest.dynamicTest("닉네임이 중복되서 회원가입 할 수 없다", () -> {
				// given
				SignUpRequest request = new SignUpRequest(
					nickname,
					"ants2345@gmail.com",
					"ants2345@",
					"ants2345@"
				);

				MemberProfile profile = profileFactory.localMemberProfile(request.getEmail(), request.getNickname(),
					request.getPassword(), null);
				NotificationPreference notificationPreference = NotificationPreference.defaultSetting();
				Member member = Member.createMember(profile, notificationPreference);

				// when
				Throwable throwable = catchThrowable(() -> service.signup(member));
				// then
				Assertions.assertThat(throwable)
					.isInstanceOf(NicknameDuplicateException.class);
			})
		);
	}

	@DisplayName("사용자는 로컬 플랫폼의 이메일이 중복되어 회원가입 할 수 없다")
	@TestFactory
	Stream<DynamicTest> duplicateLocalEmail() {
		String email = "ants1234@gmail.com";
		return Stream.of(
			DynamicTest.dynamicTest("회원가입을 정상 진행한다", () -> {
				// given
				SignUpRequest request = new SignUpRequest(
					"일개미1234",
					email,
					"ants1234@",
					"ants1234@"
				);

				MemberProfile profile = profileFactory.localMemberProfile(request.getEmail(), request.getNickname(),
					request.getPassword(), null);
				NotificationPreference notificationPreference = NotificationPreference.defaultSetting();
				Member member = Member.createMember(profile, notificationPreference);

				// when
				service.signup(member);
				// then
				Member findMember = memberRepository.findAll().stream().findAny().orElseThrow();
				Assertions.assertThat(findMember).isNotNull();
			}),
			DynamicTest.dynamicTest("이메일이 중복되서 회원가입 할 수 없다", () -> {
				// given
				SignUpRequest request = new SignUpRequest(
					"일개미2345",
					email,
					"ants1234@",
					"ants1234@"
				);

				MemberProfile profile = profileFactory.localMemberProfile(request.getEmail(), request.getNickname(),
					request.getPassword(), null);
				NotificationPreference notificationPreference = NotificationPreference.defaultSetting();
				Member member = Member.createMember(profile, notificationPreference);

				// when
				Throwable throwable = catchThrowable(() -> service.signup(member));
				// then
				Assertions.assertThat(throwable)
					.isInstanceOf(EmailDuplicateException.class);
			})
		);
	}

	@DisplayName("사용자는 프로필 이미지 사이즈를 초과하여 이미지를 업로드할 수 없다")
	@Test
	void upload_whenOverProfileImageFile_thenResponse400Error() {
		// given
		MultipartFile profileFile = TestDataFactory.createOverSizeMockProfileFile(); // 3MB
		// when
		Throwable throwable = catchThrowable(() -> service.upload(profileFile));
		// then
		assertThat(throwable)
			.isInstanceOf(MemberProfileUploadException.class);
	}
}
