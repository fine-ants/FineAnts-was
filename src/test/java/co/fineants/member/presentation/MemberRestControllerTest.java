package co.fineants.member.presentation;

import static co.fineants.api.global.success.MemberSuccessCode.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.global.errors.errorcode.ErrorCode;
import co.fineants.api.global.util.ObjectMapperUtil;
import co.fineants.member.domain.MemberRepository;
import co.fineants.member.presentation.dto.request.PasswordUpdateRequest;

class MemberRestControllerTest extends AbstractContainerBaseTest {

	private MockMvc mockMvc;

	@Autowired
	private MemberRestController controller;

	@Autowired
	private MemberRepository memberRepository;

	@BeforeEach
	void setUp() {
		mockMvc = createMockMvc(controller);
		memberRepository.save(createMember());
	}

	@DisplayName("사용자는 회원의 프로필에서 새 프로필 및 닉네임을 수정한다")
	@Test
	void changeProfile() throws Exception {
		// given
		Map<String, Object> profileInformationMap = Map.of("nickname", "일개미12345");
		MockMultipartFile profileInformation = new MockMultipartFile(
			"profileInformation",
			"profileInformation",
			MediaType.APPLICATION_JSON_VALUE,
			ObjectMapperUtil.serialize(profileInformationMap)
				.getBytes(StandardCharsets.UTF_8));

		// when & then
		mockMvc.perform(multipart(POST, "/api/profile")
				.file((MockMultipartFile)TestDataFactory.createProfileFile())
				.file(profileInformation)
				.cookie(createTokenCookies())
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("프로필이 수정되었습니다")))
			.andExpect(jsonPath("data.user.id").value(equalTo(1)))
			.andExpect(jsonPath("data.user.nickname").value(equalTo("일개미12345")))
			.andExpect(jsonPath("data.user.email").value(equalTo("dragonbead95@naver.com")))
			.andExpect(jsonPath("data.user.profileUrl").value(notNullValue()));
	}

	@DisplayName("사용자는 회원의 프로필에서 새 프로필만 수정한다")
	@Test
	void changeProfile_whenNewProfile_thenOK() throws Exception {
		// given

		// when & then
		mockMvc.perform(multipart(POST, "/api/profile")
				.file((MockMultipartFile)TestDataFactory.createProfileFile()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("프로필이 수정되었습니다")))
			.andExpect(jsonPath("data.user.id").value(equalTo(1)))
			.andExpect(jsonPath("data.user.nickname").value(equalTo("nemo1234")))
			.andExpect(jsonPath("data.user.email").value(equalTo("dragonbead95@naver.com")))
			.andExpect(jsonPath("data.user.profileUrl").value(notNullValue()));
	}

	@DisplayName("사용자는 회원의 프로필에서 기본 프로필로 수정한다")
	@Test
	void changeProfile_whenEmptyProfile_thenOK() throws Exception {
		// given

		// when & then
		mockMvc.perform(multipart(POST, "/api/profile")
				.file((MockMultipartFile)TestDataFactory.createEmptyMockMultipartFile()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("프로필이 수정되었습니다")))
			.andExpect(jsonPath("data.user.id").value(equalTo(1)))
			.andExpect(jsonPath("data.user.nickname").value(equalTo("nemo1234")))
			.andExpect(jsonPath("data.user.email").value(equalTo("dragonbead95@naver.com")))
			.andExpect(jsonPath("data.user.profileUrl").value(nullValue()));
	}

	@DisplayName("사용자는 회원의 프로필에서 프로필을 유지하고 닉네임만 변경한다")
	@Test
	void changeProfile_whenOnlyChangeNickname_thenOK() throws Exception {
		// given
		Map<String, Object> profileInformationMap = Map.of("nickname", "일개미1234");
		MockMultipartFile profileInformation = new MockMultipartFile(
			"profileInformation",
			"profileInformation",
			MediaType.APPLICATION_JSON_VALUE,
			ObjectMapperUtil.serialize(profileInformationMap)
				.getBytes(StandardCharsets.UTF_8));
		// when & then
		mockMvc.perform(multipart(POST, "/api/profile")
				.file(profileInformation))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("프로필이 수정되었습니다")))
			.andExpect(jsonPath("data.user.id").value(equalTo(1)))
			.andExpect(jsonPath("data.user.nickname").value(equalTo("일개미1234")))
			.andExpect(jsonPath("data.user.email").value(equalTo("dragonbead95@naver.com")))
			.andExpect(jsonPath("data.user.profileUrl").value(notNullValue()));
	}

	@DisplayName("사용자는 회원의 프로필에서 닉네임 입력 형식이 유효하지 않아 실패한다")
	@Test
	void changeProfile_whenInvalidNickname_thenResponse400() throws Exception {
		// given
		Map<String, Object> profileInformationMap = Map.of("nickname", "");
		MockMultipartFile profileInformation = new MockMultipartFile(
			"profileInformation",
			"profileInformation",
			MediaType.APPLICATION_JSON_VALUE,
			ObjectMapperUtil.serialize(profileInformationMap)
				.getBytes(StandardCharsets.UTF_8));
		// when & then
		mockMvc.perform(multipart(POST, "/api/profile")
				.file((MockMultipartFile)TestDataFactory.createProfileFile())
				.file(profileInformation))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data[0].field").value(equalTo("nickname")))
			.andExpect(jsonPath("data[0].defaultMessage").value(equalTo("잘못된 입력형식입니다.")));
	}

	@DisplayName("사용자는 회원의 프로필을 조회한다")
	@Test
	void readProfile() throws Exception {
		// given

		// when & then
		mockMvc.perform(get("/api/profile")
				.cookie(createTokenCookies())
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo(OK_READ_PROFILE.getMessage())))
			.andExpect(jsonPath("data.user.id").value(equalTo(1)))
			.andExpect(jsonPath("data.user.nickname").value(equalTo("nemo1234")))
			.andExpect(jsonPath("data.user.email").value(equalTo("dragonbead95@naver.com")))
			.andExpect(jsonPath("data.user.profileUrl").value(equalTo("profileUrl")))
			.andExpect(jsonPath("data.user.provider").value(equalTo("local")))
			.andExpect(jsonPath("data.user.notificationPreferences.browserNotify").value(equalTo(true)))
			.andExpect(jsonPath("data.user.notificationPreferences.targetGainNotify").value(equalTo(true)))
			.andExpect(jsonPath("data.user.notificationPreferences.maxLossNotify").value(equalTo(true)))
			.andExpect(jsonPath("data.user.notificationPreferences.targetPriceNotify").value(equalTo(true)));
	}

	@DisplayName("사용자는 비밀번호를 변경한다")
	@Test
	void changePassword() throws Exception {
		// given
		String currentPassword = "nemo1234@";
		String newPassword = "nemo2345@";
		String newPasswordConfirm = "nemo2345@";
		PasswordUpdateRequest request = new PasswordUpdateRequest(currentPassword, newPassword, newPasswordConfirm);
		// when & then
		mockMvc.perform(put("/api//account/password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(request))
				.cookie(createTokenCookies())
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo(OK_PASSWORD_CHANGED.getMessage())))
			.andExpect(jsonPath("data").value(nullValue()));
	}

	@DisplayName("사용자의 현재 비밀번호가 일치하지 않아서 비밀번호를 변경하지 못한다")
	@Test
	void changePassword_whenCurrentPasswordIsNotMatch_thenNotChangePassword() throws Exception {
		// given
		String currentPassword = "nemo2345@";
		String newPassword = "nemo2345@";
		String newPasswordConfirm = "nemo2345@";
		PasswordUpdateRequest request = new PasswordUpdateRequest(currentPassword, newPassword, newPasswordConfirm);
		// when & then
		mockMvc.perform(put("/api//account/password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(request))
				.cookie(createTokenCookies())
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.BAD_REQUEST.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.BAD_REQUEST.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(ErrorCode.PASSWORD_BAD_REQUEST.getMessage())))
			.andExpect(jsonPath("data").value(equalTo(currentPassword)));
	}
}
