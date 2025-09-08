package co.fineants.api.domain.member.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.global.errors.handler.GlobalExceptionHandler;
import co.fineants.api.global.security.oauth.resolver.MemberAuthenticationArgumentResolver;
import co.fineants.api.global.util.ObjectMapperUtil;

class MemberRestControllerTest extends AbstractContainerBaseTest {

	private MockMvc mockMvc;

	@Autowired
	private MemberRestController controller;

	@Autowired
	private GlobalExceptionHandler globalExceptionHandler;

	@Autowired
	private MemberAuthenticationArgumentResolver memberAuthenticationArgumentResolver;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MemberRepository memberRepository;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
			.setControllerAdvice(globalExceptionHandler)
			.setCustomArgumentResolvers(memberAuthenticationArgumentResolver)
			.setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
			.alwaysDo(print())
			.build();
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
				.file((MockMultipartFile)createMockMultipartFile())
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
				.file((MockMultipartFile)createMockMultipartFile()))
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
				.file((MockMultipartFile)createEmptyMockMultipartFile()))
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
				.file((MockMultipartFile)createMockMultipartFile())
				.file(profileInformation))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data[0].field").value(equalTo("nickname")))
			.andExpect(jsonPath("data[0].defaultMessage").value(equalTo("잘못된 입력형식입니다.")));
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
