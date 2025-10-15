package co.fineants.member.presentation;

import static co.fineants.api.global.success.MemberSuccessCode.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.global.util.ObjectMapperUtil;
import co.fineants.member.application.SignupService;
import co.fineants.member.application.SignupVerificationService;
import co.fineants.member.application.VerifyCodeGenerator;
import co.fineants.member.domain.Member;

class SignUpRestControllerTest extends AbstractContainerBaseTest {

	private MockMvc mockMvc;

	@Autowired
	private SignUpRestController controller;

	@Autowired
	private SignupService signupService;

	@Autowired
	private SignupVerificationService signupVerificationService;

	@Autowired
	private VerifyCodeGenerator spyVerifyCodeGenerator;

	@BeforeEach
	void setUp() {
		mockMvc = createMockMvc(controller);
	}

	@DisplayName("사용자는 일반 회원가입을 한다")
	@Test
	void signup() throws Exception {
		// given
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
				.file((MockMultipartFile)TestDataFactory.createProfileFile())
				.file(signupData))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.CREATED.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.CREATED.name())))
			.andExpect(jsonPath("message").value(equalTo(OK_SIGNUP.getMessage())));
	}

	@DisplayName("사용자는 기본 프로필 사진으로 회원가입 할 수 있다")
	@Test
	void signup_whenSkipProfileImageFile_then200OK() throws Exception {
		// given
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
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.CREATED.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.CREATED.name())))
			.andExpect(jsonPath("message").value(equalTo(OK_SIGNUP.getMessage())));
	}

	@DisplayName("사용자는 공백 문자열인 회원가입 데이터로 요청시 400 에러를 응답받는다")
	@MethodSource(value = "co.fineants.TestDataProvider#invalidEmptySignupData")
	@ParameterizedTest
	void signup_whenEmptySignupData_thenResponse400Error(String nickname, String email, String password,
		String passwordConfirm, String[] expectedFields, String[] expectedDefaultMessages) throws Exception {
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
				.file((MockMultipartFile)TestDataFactory.createProfileFile())
				.file(signupData))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.BAD_REQUEST.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.BAD_REQUEST.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data[*].field", containsInAnyOrder(expectedFields)))
			.andExpect(jsonPath("$.data[*].defaultMessage", containsInAnyOrder(expectedDefaultMessages)));
	}

	@DisplayName("사용자는 유효하지 않은 회원가입 데이터로 요청시 400 에러를 응답받는다")
	@MethodSource(value = "co.fineants.TestDataProvider#invalidSignupData")
	@ParameterizedTest
	void signup_whenInvalidSignupData_thenResponse400Error(String nickname, String email, String password,
		String passwordConfirm, String[] expectedFields, String[] expectedDefaultMessages) throws Exception {
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
				.file((MockMultipartFile)TestDataFactory.createProfileFile())
				.file(signupData))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.BAD_REQUEST.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.BAD_REQUEST.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data", hasSize(expectedFields.length)))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data[*].field", containsInAnyOrder(expectedFields)))
			.andExpect(jsonPath("$.data[*].defaultMessage", containsInAnyOrder(expectedDefaultMessages)));
	}

	@DisplayName("사용자는 중복된 닉네임으로는 회원가입 할 수 없다")
	@Test
	void signup_whenDuplicatedNickname_thenResponse400Error() throws Exception {
		// given
		saveMember("일개미1234", "ants1234@gmail.com");

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
				.file((MockMultipartFile)TestDataFactory.createProfileFile())
				.file(signupData))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("code").value(equalTo(409)))
			.andExpect(jsonPath("status").value(equalTo("Conflict")))
			.andExpect(jsonPath("message").value(equalTo("Duplicate Nickname")))
			.andExpect(jsonPath("data").value(equalTo("일개미1234")));
	}

	private void saveMember(String nickname, String email) {
		Member member = TestDataFactory.createMember(nickname, email);
		signupService.signup(member);
	}

	@DisplayName("사용자는 중복된 이메일로는 회원가입 할 수 없다")
	@Test
	void signup_whenDuplicatedEmail_thenResponse400Error() throws Exception {
		// given
		String nickname = "ants1234";
		String email = "ants1234@gmail.com";
		saveMember(nickname, email);
		Map<String, Object> profileInformationMap = Map.of(
			"nickname", "일개미1234",
			"email", email,
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
				.file((MockMultipartFile)TestDataFactory.createProfileFile())
				.file(signupData))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("code").value(equalTo(409)))
			.andExpect(jsonPath("status").value(equalTo("Conflict")))
			.andExpect(jsonPath("message").value(equalTo("Duplicate Email")))
			.andExpect(jsonPath("data").value(equalTo(email)));
	}

	@DisplayName("사용자는 비밀번호가 불일치하여 회원가입 할 수 없다")
	@Test
	void signup_whenNotMatchPasswordAndPasswordConfirm_thenResponse400Error() throws Exception {
		// given
		Map<String, Object> profileInformationMap = Map.of(
			"nickname", "일개미1234",
			"email", "dragonbead95@naver.com",
			"password", "nemo1234@",
			"passwordConfirm", "nemo1234@@");
		String json = ObjectMapperUtil.serialize(profileInformationMap);
		MockMultipartFile signupData = new MockMultipartFile(
			"signupData",
			"signupData",
			MediaType.APPLICATION_JSON_VALUE,
			json.getBytes(StandardCharsets.UTF_8));

		// when & then
		mockMvc.perform(multipart(POST, "/api/auth/signup")
				.file((MockMultipartFile)TestDataFactory.createProfileFile())
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
				.file((MockMultipartFile)TestDataFactory.createProfileFile()))
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
		String email = "ants1234@gmail.com";
		saveMember(nickname, email);

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
		String nickname = "ants1234";
		String email = "dragonbead95@naver.com";
		saveMember(nickname, email);

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
		String code = "123456";
		BDDMockito.given(spyVerifyCodeGenerator.generate())
			.willReturn(code);
		String email = "ants1234@gmail.com";
		signupVerificationService.sendSignupVerification(email);
		String body = ObjectMapperUtil.serialize(Map.of("email", email, "code", code));

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
}
