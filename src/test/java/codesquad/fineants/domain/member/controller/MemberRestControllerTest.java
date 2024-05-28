package codesquad.fineants.domain.member.controller;

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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import codesquad.fineants.ControllerTestSupport;
import codesquad.fineants.domain.member.domain.dto.request.ProfileChangeServiceRequest;
import codesquad.fineants.domain.member.domain.dto.response.ProfileChangeResponse;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.service.MemberService;
import codesquad.fineants.global.util.ObjectMapperUtil;

@WebMvcTest(controllers = MemberRestController.class)
class MemberRestControllerTest extends ControllerTestSupport {

	@MockBean
	private MemberService memberService;

	@Override
	protected Object initController() {
		return new MemberRestController(memberService);
	}

	@DisplayName("사용자는 회원의 프로필에서 새 프로필 및 닉네임을 수정한다")
	@Test
	void changeProfile() throws Exception {
		// given
		Member member = createMember("일개미12345", "profileUrl");
		given(memberService.changeProfile(ArgumentMatchers.any(ProfileChangeServiceRequest.class)))
			.willReturn(ProfileChangeResponse.from(member));

		Map<String, Object> profileInformationMap = Map.of("nickname", "일개미12345");
		MockMultipartFile profileInformation = new MockMultipartFile(
			"profileInformation",
			"profileInformation",
			MediaType.APPLICATION_JSON_VALUE,
			ObjectMapperUtil.serialize(profileInformationMap)
				.getBytes(StandardCharsets.UTF_8));

		// when & then
		mockMvc.perform(multipart(POST, "/api/profile")
				.file((MockMultipartFile)createMockMultipartFile())
				.file(profileInformation))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("프로필이 수정되었습니다")))
			.andExpect(jsonPath("data.user.id").value(equalTo(1)))
			.andExpect(jsonPath("data.user.nickname").value(equalTo("일개미12345")))
			.andExpect(jsonPath("data.user.email").value(equalTo("dragonbead95@naver.com")))
			.andExpect(jsonPath("data.user.profileUrl").value(equalTo("profileUrl")));
	}

	@DisplayName("사용자는 회원의 프로필에서 새 프로필만 수정한다")
	@Test
	void changeProfile_whenNewProfile_thenOK() throws Exception {
		// given
		Member member = createMember("일개미12345", "profileUrl");
		given(memberService.changeProfile(ArgumentMatchers.any(ProfileChangeServiceRequest.class)))
			.willReturn(ProfileChangeResponse.from(member));

		// when & then
		mockMvc.perform(multipart(POST, "/api/profile")
				.file((MockMultipartFile)createMockMultipartFile()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("프로필이 수정되었습니다")))
			.andExpect(jsonPath("data.user.id").value(equalTo(1)))
			.andExpect(jsonPath("data.user.nickname").value(equalTo("일개미12345")))
			.andExpect(jsonPath("data.user.email").value(equalTo("dragonbead95@naver.com")))
			.andExpect(jsonPath("data.user.profileUrl").value(equalTo("profileUrl")));
	}

	@DisplayName("사용자는 회원의 프로필에서 기본 프로필로 수정한다")
	@Test
	void changeProfile_whenEmptyProfile_thenOK() throws Exception {
		// given
		Member member = createMember("일개미12345", null);
		given(memberService.changeProfile(ArgumentMatchers.any(ProfileChangeServiceRequest.class)))
			.willReturn(ProfileChangeResponse.from(member));

		// when & then
		mockMvc.perform(multipart(POST, "/api/profile")
				.file((MockMultipartFile)createEmptyMockMultipartFile()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("프로필이 수정되었습니다")))
			.andExpect(jsonPath("data.user.id").value(equalTo(1)))
			.andExpect(jsonPath("data.user.nickname").value(equalTo("일개미12345")))
			.andExpect(jsonPath("data.user.email").value(equalTo("dragonbead95@naver.com")))
			.andExpect(jsonPath("data.user.profileUrl").value(equalTo(null)));
	}

	@DisplayName("사용자는 회원의 프로필에서 프로필을 유지하고 닉네임만 변경한다")
	@Test
	void changeProfile_whenOnlyChangeNickname_thenOK() throws Exception {
		// given
		Member member = createMember("일개미1234", "profileUrl");
		given(memberService.changeProfile(ArgumentMatchers.any(ProfileChangeServiceRequest.class)))
			.willReturn(ProfileChangeResponse.from(member));

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
			.andExpect(jsonPath("data.user.profileUrl").value(equalTo("profileUrl")));
	}

	@DisplayName("사용자는 회원의 프로필에서 닉네임 입력 형식이 유효하지 않아 실패한다")
	@Test
	void changeProfile_whenInvalidNickname_thenResponse400() throws Exception {
		// given
		Member member = createMember("일개미1234", "profileUrl");
		given(memberService.changeProfile(ArgumentMatchers.any(ProfileChangeServiceRequest.class)))
			.willReturn(ProfileChangeResponse.from(member));

		Map<String, Object> profileInformationMap = Map.of("nickname", "");
		MockMultipartFile profileInformation = new MockMultipartFile(
			"profileInformation",
			"profileInformation",
			MediaType.APPLICATION_JSON_VALUE,
			ObjectMapperUtil.serialize(profileInformationMap)
				.getBytes(StandardCharsets.UTF_8));
		// when & then
		mockMvc.perform(multipart(POST, "/api/profile")
				.file((MockMultipartFile)createMockMultipartFile())
				.file(profileInformation))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data[0].field").value(equalTo("nickname")))
			.andExpect(jsonPath("data[0].defaultMessage").value(equalTo("잘못된 입력형식입니다.")));
	}

	private Member createMember(String nickname, String profileUrl) {
		return Member.builder()
			.id(1L)
			.nickname(nickname)
			.email("dragonbead95@naver.com")
			.provider("local")
			.password("password")
			.profileUrl(profileUrl)
			.build();
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

	public MultipartFile createEmptyMockMultipartFile() {
		return new MockMultipartFile("profileImageFile", new byte[] {});
	}
}
