package co.fineants.api.domain.fcm.controller;

import static co.fineants.api.global.success.FcmSuccessCode.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.fcm.domain.dto.request.FcmRegisterRequest;
import co.fineants.api.domain.fcm.domain.entity.FcmToken;
import co.fineants.api.domain.fcm.repository.FcmRepository;
import co.fineants.api.domain.fcm.service.FcmService;
import co.fineants.api.global.util.ObjectMapperUtil;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;

class FcmRestControllerTest extends AbstractContainerBaseTest {

	@Autowired
	private FcmService mockedFcmService;

	@Autowired
	private FcmRestController controller;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private FcmRepository fcmRepository;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = createMockMvc(controller);
	}

	@DisplayName("사용자는 FCM 토큰을 등록한다")
	@Test
	void registerFcmToken() throws Exception {
		// given
		memberRepository.save(TestDataFactory.createMember());
		FcmRegisterRequest request = new FcmRegisterRequest("fcmToken");

		// when & then
		mockMvc.perform(post("/api/fcm/tokens")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.CREATED.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.CREATED.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(CREATED_FCM.getMessage())))
			.andExpect(jsonPath("data.fcmTokenId").value(greaterThan(0)));
	}

	@DisplayName("사용자는 유효하지 않은 형식의 토큰을 전달하여 등록할 수 없다")
	@Test
	void registerToken_whenInvalidFcmToken_thenResponse400Error() throws Exception {
		// given
		memberRepository.save(TestDataFactory.createMember());
		FcmRegisterRequest request = new FcmRegisterRequest(null);

		// when
		mockMvc.perform(post("/api/fcm/tokens")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.BAD_REQUEST.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.BAD_REQUEST.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data[*].field", containsInAnyOrder("fcmToken")))
			.andExpect(jsonPath("data[*].defaultMessage", containsInAnyOrder("FCM 토큰은 필수 정보입니다")));
	}

	@DisplayName("사용자는 FCM 토큰을 삭제한다")
	@Test
	void deleteFcmToken() throws Exception {
		// given
		Member member = memberRepository.save(TestDataFactory.createMember());
		FcmToken fcmToken = FcmToken.create(member, "fcmToken");
		fcmRepository.save(fcmToken);

		// when & then
		mockMvc.perform(delete("/api/fcm/tokens/{fcmTokenId}", fcmToken.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(OK_DELETE_FCM.getMessage())))
			.andExpect(jsonPath("data").value(nullValue()));
	}
}
