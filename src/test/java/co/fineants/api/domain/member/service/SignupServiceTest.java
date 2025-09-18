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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.MemberProfile;
import co.fineants.api.domain.member.domain.entity.NotificationPreference;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.global.errors.exception.business.EmailInvalidInputException;
import co.fineants.api.global.errors.exception.business.NicknameDuplicateException;
import co.fineants.api.global.errors.exception.business.NicknameInvalidInputException;

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

	private static Stream<Arguments> invalidEmailSource() {
		return Stream.of(
			Arguments.of(""), // 빈 문자열
			Arguments.of("invalidEmail"), // 잘못된 형식의 이메일
			Arguments.of("invalid@Email"), // 도메인 부분이 없는 이메일
			Arguments.of("invalidEmail@.com"), // 도메인 부분이 없는 이메일
			Arguments.of("invalidEmail@domain..com"), // 도메인 부분에 '.'이 연속된 이메일
			Arguments.of((Object)null) // null 값
		);
	}

	private static Stream<Arguments> invalidNicknameSource() {
		return Stream.of(
			Arguments.of(""), // 빈 문자열
			Arguments.of("a"), // 너무 짧은 닉네임
			Arguments.of("a".repeat(21)), // 너무 긴 닉네임
			Arguments.of("invalid nickname"), // 공백이 포함된 닉네임
			Arguments.of("invalid@nickname"), // 특수문자가 포함된 닉네임
			Arguments.of((Object)null) // null 값
		);
	}

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
		MemberProfile profile = MemberProfile.localMemberProfile("ants1@gmail.com", "ants1", "ants1234@", null);
		Member member = Member.localMember(profile);
		member.setNotificationPreference(NotificationPreference.defaultSetting());
		
		// when
		service.signup(member);
		// then
		int memberSize = memberRepository.findAll().size();
		assertThat(memberSize).isEqualTo(1);
	}

	@DisplayName("사용자는 유효하지 않은 형식의 이메일이 주어졌을때 회원가입에 실패한다")
	@ParameterizedTest
	@MethodSource(value = "invalidEmailSource")
	void givenInvalidEmail_whenValidateEmail_thenFailSignup(String email) {
		// given
		MemberProfile profile = MemberProfile.localMemberProfile(email, "ants1", "ants1234@", null);
		Member member = Member.localMember(profile);
		// when
		Throwable throwable = catchThrowable(() -> service.signup(member));
		// then
		assertThat(throwable)
			.isInstanceOf(EmailInvalidInputException.class);
	}

	@DisplayName("사용자는 유효하지 않은 형식의 닉네임이 주어졌을때 회원가입에 실패한다")
	@ParameterizedTest
	@MethodSource(value = "invalidNicknameSource")
	void givenInvalidNickname_whenValidateNickname_thenFailSignup(String nickname) {
		// given
		MemberProfile profile = MemberProfile.localMemberProfile("ants1234@gmail.com", nickname, "ants1234@", null);
		Member member = Member.localMember(profile);
		// when
		Throwable throwable = catchThrowable(() -> service.signup(member));
		// then
		assertThat(throwable)
			.isInstanceOf(NicknameInvalidInputException.class);
	}

	@DisplayName("사용자는 이미 존재하는 닉네임을 가지고 회원가입 할 수 없다.")
	@Test
	void givenDuplicatedNickname_whenValidateNickname_thenFailSignup() {
		// given
		String nickname = "ants1";
		MemberProfile profile = MemberProfile.localMemberProfile("ants1234@gmail.com", nickname, "ants1234@", null);
		Member member = Member.localMember(profile);
		member.setNotificationPreference(NotificationPreference.defaultSetting());
		memberRepository.save(member);

		MemberProfile otherProfile = MemberProfile.localMemberProfile("ants4567@gmail.com", nickname, "ants4567@",
			null);
		Member otherMember = Member.localMember(otherProfile);
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
}
