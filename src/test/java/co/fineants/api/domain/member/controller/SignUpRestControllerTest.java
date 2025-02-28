package co.fineants.api.domain.member.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import co.fineants.api.domain.member.domain.dto.request.SignUpServiceRequest;
import co.fineants.api.domain.member.domain.dto.response.SignUpServiceResponse;
import co.fineants.api.domain.member.service.MemberService;
import co.fineants.api.global.errors.errorcode.MemberErrorCode;
import co.fineants.api.global.errors.exception.BadRequestException;
import co.fineants.api.global.util.ObjectMapperUtil;
import co.fineants.support.controller.ControllerTestSupport;

public class SignUpRestControllerTest extends ControllerTestSupport {

	@Autowired
	private MemberService mockedMemberService;

	@Override
	protected Object initController() {
		return new SignUpRestController(mockedMemberService);
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
		given(mockedMemberService.signup(ArgumentMatchers.any(SignUpServiceRequest.class)))
			.willThrow(new BadRequestException(MemberErrorCode.REDUNDANT_NICKNAME));

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
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("닉네임이 중복되었습니다")));
	}

	@DisplayName("사용자는 중복된 이메일로는 회원가입 할 수 없다")
	@Test
	void signup_whenDuplicatedEmail_thenResponse400Error() throws Exception {
		// given
		given(mockedMemberService.signup(ArgumentMatchers.any(SignUpServiceRequest.class)))
			.willThrow(new BadRequestException(MemberErrorCode.REDUNDANT_EMAIL));

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
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("이메일이 중복되었습니다")));
	}

	@DisplayName("사용자는 비밀번호가 불일치하여 회원가입 할 수 없다")
	@Test
	void signup_whenNotMatchPasswordAndPasswordConfirm_thenResponse400Error() throws Exception {
		// given
		given(mockedMemberService.signup(ArgumentMatchers.any(SignUpServiceRequest.class)))
			.willThrow(new BadRequestException(MemberErrorCode.PASSWORD_CHECK_FAIL));

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
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("비밀번호가 일치하지 않습니다")));
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

	@DisplayName("사용자는 회원가입 과정중 닉네임이 중복되어 400 응답을 받는다")
	@Test
	void nicknameDuplicationCheck_whenDuplicatedNickname_thenResponse400Error() throws Exception {
		// given
		doThrow(new BadRequestException(MemberErrorCode.REDUNDANT_NICKNAME))
			.when(mockedMemberService)
			.checkNickname(anyString());
		String nickname = "일개미1234";

		// when & then
		mockMvc.perform(get("/api/auth/signup/duplicationcheck/nickname/{nickname}", nickname))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("닉네임이 중복되었습니다")));
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
		doThrow(new BadRequestException(MemberErrorCode.REDUNDANT_EMAIL))
			.when(mockedMemberService)
			.checkEmail(anyString());

		// when & then
		mockMvc.perform(get("/api/auth/signup/duplicationcheck/email/{email}", email))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("이메일이 중복되었습니다")));
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
