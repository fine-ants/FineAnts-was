package co.fineants.api.domain.member.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import co.fineants.api.domain.member.domain.dto.request.SignUpServiceRequest;
import co.fineants.api.domain.member.domain.dto.response.SignUpServiceResponse;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.factory.MemberFactory;
import co.fineants.api.domain.member.domain.factory.MemberProfileFactory;
import co.fineants.api.domain.member.domain.factory.MimeMessageFactory;
import co.fineants.api.domain.member.domain.rule.EmailValidator;
import co.fineants.api.domain.member.domain.rule.NicknameValidator;
import co.fineants.api.domain.member.domain.rule.PasswordValidator;
import co.fineants.api.domain.member.service.MemberService;
import co.fineants.api.domain.member.service.SignupService;
import co.fineants.api.domain.member.service.VerifyCodeGenerator;
import co.fineants.api.domain.member.service.VerifyCodeManagementService;
import co.fineants.api.global.errors.exception.business.EmailDuplicateException;
import co.fineants.api.global.errors.exception.business.NicknameDuplicateException;
import co.fineants.api.global.errors.exception.business.PasswordAuthenticationException;
import co.fineants.api.global.util.ObjectMapperUtil;
import co.fineants.api.infra.mail.EmailService;
import co.fineants.support.controller.ControllerTestSupport;

public class SignUpRestControllerTest extends ControllerTestSupport {

	private SignupService signupService;

	@Autowired
	private MemberService mockedMemberService;

	private NicknameValidator nicknameValidator;
	private EmailValidator emailValidator;

	@Override
	protected Object initController() {
		signupService = mock(SignupService.class);
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		MemberProfileFactory memberProfileFactory = new MemberProfileFactory();
		MemberFactory memberFactory = new MemberFactory();
		nicknameValidator = Mockito.mock(NicknameValidator.class);
		emailValidator = Mockito.mock(EmailValidator.class);
		PasswordValidator passwordValidator = new PasswordValidator();
		VerifyCodeGenerator verifyCodeGenerator = new VerifyCodeGenerator(6, 1000000);
		VerifyCodeManagementService verifyCodeManagementService = mock(VerifyCodeManagementService.class);
		MimeMessageFactory mimeMessageFactory = mock(MimeMessageFactory.class);
		EmailService emailService = mock(EmailService.class);
		return new SignUpRestController(signupService, mockedMemberService, passwordEncoder, memberProfileFactory,
			memberFactory, nicknameValidator, emailValidator, passwordValidator, verifyCodeGenerator,
			verifyCodeManagementService, mimeMessageFactory, emailService);
	}

	@DisplayName("사용자는 일반 회원가입을 한다")
	@Test
	void signup() throws Exception {
		// given
		given(mockedMemberService.signup(ArgumentMatchers.any(SignUpServiceRequest.class)))
			.willReturn(SignUpServiceResponse.from(createMember()));

		Map<String, Object> profileInformationMap = Map.of(
			"nickname", "일개미1234",
			"email", "dragonbead95@naver.com",
			"password", "nemo1234@",
			"passwordConfirm", "nemo1234@");
		String json = ObjectMapperUtil.serialize(profileInformationMap);
		MockMultipartFile signupData = new MockMultipartFile(
			"signupData",
			"signupData",
			MediaType.APPLICATION_JSON_VALUE,
			json.getBytes(StandardCharsets.UTF_8));

		// when & then
		mockMvc.perform(multipart(POST, "/api/auth/signup")
				.file((MockMultipartFile)createMockMultipartFile())
				.file(signupData))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo("회원가입이 완료되었습니다")));
	}

	@DisplayName("사용자는 기본 프로필 사진으로 회원가입 할 수 있다")
	@Test
	void signup_whenSkipProfileImageFile_then200OK() throws Exception {
		// given
		given(mockedMemberService.signup(ArgumentMatchers.any(SignUpServiceRequest.class)))
			.willReturn(SignUpServiceResponse.from(createMember()));

		Map<String, Object> profileInformationMap = Map.of(
			"nickname", "일개미1234",
			"email", "dragonbead95@naver.com",
			"password", "nemo1234@",
			"passwordConfirm", "nemo1234@");
		String json = ObjectMapperUtil.serialize(profileInformationMap);
		MockMultipartFile signupData = new MockMultipartFile(
			"signupData",
			"signupData",
			MediaType.APPLICATION_JSON_VALUE,
			json.getBytes(StandardCharsets.UTF_8));
		// when & then
		mockMvc.perform(multipart(POST, "/api/auth/signup")
				.file(signupData))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo("회원가입이 완료되었습니다")));
	}

	@DisplayName("사용자는 유효하지 않은 회원가입 데이터로 요청시 400 에러를 응답받는다")
	@MethodSource(value = "invalidSignupData")
	@ParameterizedTest
	void signup_whenInvalidSignupData_thenResponse400Error(String nickname, String email, String password,
		String passwordConfirm) throws Exception {
		// given
		Map<String, Object> profileInformationMap = Map.of(
			"nickname", nickname,
			"email", email,
			"password", password,
			"passwordConfirm", passwordConfirm);
		String json = ObjectMapperUtil.serialize(profileInformationMap);
		MockMultipartFile signupData = new MockMultipartFile(
			"signupData",
			"signupData",
			MediaType.APPLICATION_JSON_VALUE,
			json.getBytes(StandardCharsets.UTF_8));

		// when & then
		mockMvc.perform(multipart(POST, "/api/auth/signup")
				.file((MockMultipartFile)createMockMultipartFile())
				.file(signupData))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")));
	}

	@DisplayName("사용자는 중복된 닉네임으로는 회원가입 할 수 없다")
	@Test
	void signup_whenDuplicatedNickname_thenResponse400Error() throws Exception {
		// given
		willThrow(new NicknameDuplicateException("일개미1234"))
			.given(signupService)
			.signup(ArgumentMatchers.any(Member.class));

		Map<String, Object> profileInformationMap = Map.of(
			"nickname", "일개미1234",
			"email", "dragonbead95@naver.com",
			"password", "nemo1234@",
			"passwordConfirm", "nemo1234@");
		String json = ObjectMapperUtil.serialize(profileInformationMap);
		MockMultipartFile signupData = new MockMultipartFile(
			"signupData",
			"signupData",
			MediaType.APPLICATION_JSON_VALUE,
			json.getBytes(StandardCharsets.UTF_8));

		// when & then
		mockMvc.perform(multipart(POST, "/api/auth/signup")
				.file((MockMultipartFile)createMockMultipartFile())
				.file(signupData))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("code").value(equalTo(409)))
			.andExpect(jsonPath("status").value(equalTo("Conflict")))
			.andExpect(jsonPath("message").value(equalTo("Duplicate Nickname")))
			.andExpect(jsonPath("data").value(equalTo("일개미1234")));
	}

	@DisplayName("사용자는 중복된 이메일로는 회원가입 할 수 없다")
	@Test
	void signup_whenDuplicatedEmail_thenResponse400Error() throws Exception {
		// given
		willThrow(new EmailDuplicateException("dragonbead95@naver.com"))
			.given(signupService)
			.signup(ArgumentMatchers.any(Member.class));

		Map<String, Object> profileInformationMap = Map.of(
			"nickname", "일개미1234",
			"email", "dragonbead95@naver.com",
			"password", "nemo1234@",
			"passwordConfirm", "nemo1234@");
		String json = ObjectMapperUtil.serialize(profileInformationMap);
		MockMultipartFile signupData = new MockMultipartFile(
			"signupData",
			"signupData",
			MediaType.APPLICATION_JSON_VALUE,
			json.getBytes(StandardCharsets.UTF_8));

		// when & then
		mockMvc.perform(multipart(POST, "/api/auth/signup")
				.file((MockMultipartFile)createMockMultipartFile())
				.file(signupData))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("code").value(equalTo(409)))
			.andExpect(jsonPath("status").value(equalTo("Conflict")))
			.andExpect(jsonPath("message").value(equalTo("Duplicate Email")))
			.andExpect(jsonPath("data").value(equalTo("dragonbead95@naver.com")));
	}

	@DisplayName("사용자는 비밀번호가 불일치하여 회원가입 할 수 없다")
	@Test
	void signup_whenNotMatchPasswordAndPasswordConfirm_thenResponse400Error() throws Exception {
		// given
		willThrow(new PasswordAuthenticationException())
			.given(signupService)
			.signup(ArgumentMatchers.any(Member.class));

		Map<String, Object> profileInformationMap = Map.of(
			"nickname", "일개미1234",
			"email", "dragonbead95@naver.com",
			"password", "nemo1234@",
			"passwordConfirm", "nemo1234@");
		String json = ObjectMapperUtil.serialize(profileInformationMap);
		MockMultipartFile signupData = new MockMultipartFile(
			"signupData",
			"signupData",
			MediaType.APPLICATION_JSON_VALUE,
			json.getBytes(StandardCharsets.UTF_8));

		// when & then
		mockMvc.perform(multipart(POST, "/api/auth/signup")
				.file((MockMultipartFile)createMockMultipartFile())
				.file(signupData))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("code").value(equalTo(401)))
			.andExpect(jsonPath("status").value(equalTo("Unauthorized")))
			.andExpect(jsonPath("message").value(equalTo("Unauthenticated Password")))
			.andExpect(jsonPath("data").value(equalTo(Strings.EMPTY)));
	}

	@DisplayName("사용자는 signupData 필드 없이 회원가입 할 수 없다")
	@Test
	void signup_whenNotExistSignupDataField_thenResponse400Error() throws Exception {
		// given

		// when & then
		mockMvc.perform(multipart(POST, "/api/auth/signup")
				.file((MockMultipartFile)createMockMultipartFile()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("Required part 'signupData' is not present.")));
	}

	@DisplayName("사용자는 회원가입 과정중 닉네임이 중복되었는지 검사할 수 있다")
	@Test
	void nicknameDuplicationCheck() throws Exception {
		// given
		String nickname = "일개미1234";
		// when & then
		mockMvc.perform(get("/api/auth/signup/duplicationcheck/nickname/{nickname}", nickname))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("닉네임이 사용가능합니다")));
	}

	@DisplayName("사용자는 회원가입 과정중 닉네임이 중복되어 409 응답을 받는다")
	@Test
	void nicknameDuplicationCheck_whenDuplicatedNickname_thenResponse400Error() throws Exception {
		// given
		String nickname = "일개미1234";
		doThrow(new NicknameDuplicateException(nickname))
			.when(nicknameValidator)
			.validate(nickname);

		// when & then
		mockMvc.perform(get("/api/auth/signup/duplicationcheck/nickname/{nickname}", nickname))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("code").value(equalTo(409)))
			.andExpect(jsonPath("status").value(equalTo("Conflict")))
			.andExpect(jsonPath("message").value(equalTo("Duplicate Nickname")))
			.andExpect(jsonPath("data").value(equalTo(nickname)));
	}

	@DisplayName("사용자는 로컬 이메일이 중복되었는지 검사한다")
	@Test
	void emailDuplicationCheck() throws Exception {
		// given
		String email = "dragonbead95@naver.com";

		// when & then
		mockMvc.perform(get("/api/auth/signup/duplicationcheck/email/{email}", email))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("이메일이 사용가능합니다")));
	}

	@DisplayName("사용자는 로컬 이메일이 중복되어 400 에러를 응답받는다")
	@Test
	void emailDuplicationCheck_whenDuplicatedEmail_thenResponse400Error() throws Exception {
		// given
		String email = "dragonbead95@naver.com";
		doThrow(new EmailDuplicateException(email))
			.when(emailValidator)
			.validate(email);

		// when & then
		mockMvc.perform(get("/api/auth/signup/duplicationcheck/email/{email}", email))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("code").value(equalTo(409)))
			.andExpect(jsonPath("status").value(equalTo("Conflict")))
			.andExpect(jsonPath("message").value(equalTo("Duplicate Email")))
			.andExpect(jsonPath("data").value(equalTo(email)));
	}

	@DisplayName("사용자는 이메일을 전달하고 이메일로 검증 코드를 받는다")
	@Test
	void sendVerifyCode() throws Exception {
		// given
		String body = ObjectMapperUtil.serialize(Map.of("email", "dragonbead95@naver.com"));

		// when & then
		mockMvc.perform(post("/api/auth/signup/verifyEmail")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("이메일로 검증 코드를 전송하였습니다")));
	}

	@DisplayName("사용자는 유효하지 않은 형식의 이메일을 가지고 검증 코드를 받을 수 없다")
	@Test
	void sendVerifyCode_whenInvalidEmail_thenResponse400Error() throws Exception {
		// given
		String body = ObjectMapperUtil.serialize(Map.of("email", ""));

		// when & then
		mockMvc.perform(post("/api/auth/signup/verifyEmail")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")));
	}

	@DisplayName("사용자는 검증코드를 제출하고 검증 완료 응답을 받는다")
	@Test
	void checkVerifyCode() throws Exception {
		// given
		String body = ObjectMapperUtil.serialize(Map.of("email", "dragonbead95@naver.com", "code", "123456"));

		// when & then
		mockMvc.perform(post("/api/auth/signup/verifyCode")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("일치하는 인증번호 입니다")));
	}

	@DisplayName("사용자는 유효하지 않은 이메일과 검증코드를 제출하고 에러 응답을 받는다")
	@Test
	void checkVerifyCode_whenInvalidEmailAndCode_thenResponse400Error() throws Exception {
		// given
		String body = ObjectMapperUtil.serialize(Map.of("email", "", "code", ""));

		// when & then
		mockMvc.perform(post("/api/auth/signup/verifyCode")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")));
	}

	public MultipartFile createMockMultipartFile() {
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

	public static Stream<Arguments> invalidSignupData() {
		return Stream.of(
			Arguments.of("", "", "", ""),
			Arguments.of("a", "a", "a", "a")
		);
	}
}
