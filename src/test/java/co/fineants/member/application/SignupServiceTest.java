package co.fineants.member.application;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;

import co.fineants.api.global.errors.exception.business.EmailDuplicateException;
import co.fineants.api.global.errors.exception.business.NicknameDuplicateException;
import co.fineants.api.infra.s3.service.WriteProfileImageFileService;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberEmail;
import co.fineants.member.domain.MemberPassword;
import co.fineants.member.domain.MemberPasswordEncoder;
import co.fineants.member.domain.MemberProfile;
import co.fineants.member.domain.MemberRepository;
import co.fineants.member.domain.Nickname;
import co.fineants.member.domain.NotificationPreference;
import co.fineants.member.presentation.dto.request.SignUpRequest;
import co.fineants.role.domain.Role;
import co.fineants.role.domain.RoleRepository;

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
	private MemberPasswordEncoder memberPasswordEncoder;

	@Autowired
	private WriteProfileImageFileService writeProfileImageFileService;

	@Autowired
	private RoleRepository roleRepository;

	@NotNull
	private MemberProfile createMemberProfile(SignUpRequest request, String profileUrl) {
		MemberEmail memberEmail = new MemberEmail(request.getEmail());
		Nickname nickname = new Nickname(request.getNickname());
		MemberPassword memberPassword = new MemberPassword(request.getPassword(), memberPasswordEncoder);
		return MemberProfile.localMemberProfile(memberEmail, nickname, memberPassword, profileUrl);
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

	@Transactional
	@DisplayName("사용자는 회원가입시 회원 정보를 저장한다")
	@Test
	void should_saveMember_whenSignup() {
		// given
		MemberEmail memberEmail = new MemberEmail("ants1@gmail.com");
		Nickname nickname = new Nickname("ants1");
		String rawPassword = "ants1234@";
		MemberPassword memberPassword = new MemberPassword(rawPassword, memberPasswordEncoder);

		MemberProfile profile = MemberProfile.localMemberProfile(memberEmail, nickname, memberPassword, null);
		NotificationPreference notificationPreference = NotificationPreference.defaultSetting();

		Role userRole = roleRepository.findRoleByRoleName("ROLE_USER").orElseThrow();
		Set<Long> roleIds = Set.of(userRole.getId());
		Member member = Member.createMember(profile, notificationPreference, roleIds);

		// when
		service.signup(member);
		// then
		Member findMember = memberRepository.findAll().stream().findAny().orElseThrow();
		assertThat(findMember).isNotNull();
		assertThat(findMember.getRoleIds())
			.hasSize(1)
			.containsExactlyInAnyOrder(userRole.getId());
	}

	@DisplayName("사용자는 이미 존재하는 닉네임을 가지고 회원가입 할 수 없다.")
	@Test
	void givenDuplicatedNickname_whenValidateNickname_thenFailSignup() {
		// given
		MemberEmail memberEmail = new MemberEmail("ants1234@gmail.com");
		Nickname nickname = new Nickname("ants1234");
		String rawPassword = "ants1234@";
		MemberPassword memberPassword = new MemberPassword(rawPassword, memberPasswordEncoder);
		MemberProfile profile = MemberProfile.localMemberProfile(memberEmail, nickname, memberPassword,
			null);
		NotificationPreference notificationPreference = NotificationPreference.defaultSetting();
		Role userRole = roleRepository.findRoleByRoleName("ROLE_USER").orElseThrow();
		Set<Long> roleIds = Set.of(userRole.getId());
		Member member = Member.createMember(profile, notificationPreference, roleIds);
		memberRepository.save(member);

		MemberEmail changeMemberEmail = new MemberEmail("ants4567@gmail.com");
		rawPassword = "ants4567@";
		memberPassword = new MemberPassword(rawPassword, memberPasswordEncoder);
		MemberProfile otherProfile = MemberProfile.localMemberProfile(changeMemberEmail, nickname, memberPassword,
			null);

		Member otherMember = Member.createMember(otherProfile, notificationPreference, roleIds);
		// when
		Throwable throwable = catchThrowable(() -> service.signup(otherMember));
		// then
		assertThat(throwable)
			.isInstanceOf(NicknameDuplicateException.class);
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
		String profileUrl = writeProfileImageFileService.upload(profileImageFile);
		MemberProfile profile = createMemberProfile(request, profileUrl);
		NotificationPreference notificationPreference = NotificationPreference.defaultSetting();
		Role userRole = roleRepository.findRoleByRoleName("ROLE_USER").orElseThrow();
		Set<Long> roleIds = Set.of(userRole.getId());
		Member member = Member.createMember(profile, notificationPreference, roleIds);

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
		MemberProfile profile = createMemberProfile(request, profileUrl);
		NotificationPreference notificationPreference = NotificationPreference.defaultSetting();
		Role userRole = roleRepository.findRoleByRoleName("ROLE_USER").orElseThrow();
		Set<Long> roleIds = Set.of(userRole.getId());
		Member member = Member.createMember(profile, notificationPreference, roleIds);
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

				MemberProfile profile = createMemberProfile(request, null);
				NotificationPreference notificationPreference = NotificationPreference.defaultSetting();
				Role userRole = roleRepository.findRoleByRoleName("ROLE_USER").orElseThrow();
				Set<Long> roleIds = Set.of(userRole.getId());
				Member member = Member.createMember(profile, notificationPreference, roleIds);

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

				MemberProfile profile = createMemberProfile(request, null);
				NotificationPreference notificationPreference = NotificationPreference.defaultSetting();
				Role userRole = roleRepository.findRoleByRoleName("ROLE_USER").orElseThrow();
				Set<Long> roleIds = Set.of(userRole.getId());
				Member member = Member.createMember(profile, notificationPreference, roleIds);

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

				MemberProfile profile = createMemberProfile(request, null);
				NotificationPreference notificationPreference = NotificationPreference.defaultSetting();
				Role userRole = roleRepository.findRoleByRoleName("ROLE_USER").orElseThrow();
				Set<Long> roleIds = Set.of(userRole.getId());
				Member member = Member.createMember(profile, notificationPreference, roleIds);

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

				MemberProfile profile = createMemberProfile(request, null);
				NotificationPreference notificationPreference = NotificationPreference.defaultSetting();
				Role userRole = roleRepository.findRoleByRoleName("ROLE_USER").orElseThrow();
				Set<Long> roleIds = Set.of(userRole.getId());
				Member member = Member.createMember(profile, notificationPreference, roleIds);

				// when
				Throwable throwable = catchThrowable(() -> service.signup(member));
				// then
				Assertions.assertThat(throwable)
					.isInstanceOf(EmailDuplicateException.class);
			})
		);
	}
}
