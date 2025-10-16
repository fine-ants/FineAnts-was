package co.fineants.api.domain.portfolio.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.portfolio.domain.dto.request.PortfolioCreateRequest;
import co.fineants.api.domain.portfolio.domain.dto.request.PortfolioModifyRequest;
import co.fineants.api.domain.portfolio.domain.dto.response.PortfolioModifyResponse;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.portfolio.service.PortfolioService;
import co.fineants.api.global.success.PortfolioSuccessCode;
import co.fineants.api.global.util.ObjectMapperUtil;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;

class PortFolioRestControllerTest extends AbstractContainerBaseTest {

	@Autowired
	private PortfolioService mockedPortfolioService;

	@Autowired
	private PortFolioRestController controller;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;
	private Member member;

	@BeforeEach
	void setUp() {
		mockMvc = createMockMvc(controller);
		member = memberRepository.save(TestDataFactory.createMember());
	}

	@DisplayName("사용자는 포트폴리오를 생성한다")
	@ParameterizedTest
	@MethodSource(value = "co.fineants.TestDataProvider#createPortfolioSource")
	void createPortfolio_whenAddPortfolio_thenSavePortfolio(Long budget, Long targetGain, Long maximumLoss) throws
		Exception {
		// given
		PortfolioCreateRequest request = PortfolioCreateRequest.create(
			"내꿈은 워렌버핏",
			"토스증권",
			Money.won(budget),
			Money.won(targetGain),
			Money.won(maximumLoss)
		);

		// when & then
		mockMvc.perform(post("/api/portfolios")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.CREATED.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.CREATED.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(PortfolioSuccessCode.CREATED_ADD_PORTFOLIO.getMessage())))
			.andExpect(jsonPath("data.portfolioId").value(greaterThan(0)));
	}

	@DisplayName("사용자는 포트폴리오 추가시 유효하지 않은 입력 정보로 추가할 수 없다")
	@ParameterizedTest
	@MethodSource(value = "co.fineants.TestDataProvider#invalidCreatePortfolioSource")
	void addPortfolioWithInvalidInput(String name, String securitiesFirm, Long budget, Long targetGain,
		Long maximumLoss) throws Exception {
		// given
		PortfolioCreateRequest request = PortfolioCreateRequest.create(
			name,
			securitiesFirm,
			Money.won(budget),
			Money.won(targetGain),
			Money.won(maximumLoss)
		);
		// when & then
		mockMvc.perform(post("/api/portfolios")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.BAD_REQUEST.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.BAD_REQUEST.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data").isArray())
			.andExpect(jsonPath("data[*].field", containsInAnyOrder(
				"securitiesFirm", "maximumLoss", "name", "targetGain", "name")))
			.andExpect(jsonPath("data[*].defaultMessage", containsInAnyOrder(
				"증권사는 필수 정보입니다",
				"금액은 0포함 양수여야 합니다",
				"포트폴리오 이름은 필수 정보입니다",
				"금액은 0포함 양수여야 합니다",
				"유효하지 않은 포트폴리오 이름입니다."
			)));
	}

	@DisplayName("사용자는 자신의 포트폴리오 목록을 조회한다")
	@Test
	void searchMyAllPortfolios() throws Exception {
		// given
		Portfolio portfolio = portfolioRepository.save(TestDataFactory.createPortfolio(member));

		// when & then
		mockMvc.perform(get("/api/portfolios"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(PortfolioSuccessCode.OK_SEARCH_PORTFOLIOS.getMessage())))
			.andExpect(jsonPath("data.portfolios").isArray())
			.andExpect(jsonPath("data.portfolios[0].id").value(equalTo(portfolio.getId().intValue())))
			.andExpect(jsonPath("data.portfolios[0].securitiesFirm").value(equalTo("토스증권")))
			.andExpect(jsonPath("data.portfolios[0].name").value(equalTo("내꿈은 워렌버핏")))
			.andExpect(jsonPath("data.portfolios[0].budget").value(equalTo(1000000)))
			.andExpect(jsonPath("data.portfolios[0].totalGain").value(equalTo(0)))
			.andExpect(jsonPath("data.portfolios[0].totalGainRate").value(equalTo(0.0)))
			.andExpect(jsonPath("data.portfolios[0].dailyGain").value(equalTo(0)))
			.andExpect(jsonPath("data.portfolios[0].dailyGainRate").value(equalTo(0.0)))
			.andExpect(jsonPath("data.portfolios[0].currentValuation")
				.value(equalTo(0)))
			.andExpect(jsonPath("data.portfolios[0].expectedMonthlyDividend")
				.value(equalTo(0)))
			.andExpect(jsonPath("data.portfolios[0].numShares")
				.value(equalTo(0)))
			.andExpect(jsonPath("data.portfolios[0].dateCreated").value(notNullValue()));
	}

	@DisplayName("사용자는 포트폴리오 수정을 요청한다")
	@CsvSource(value = {"1000000,1500000,900000", "0,0,0", "0,1500000,900000"})
	@ParameterizedTest
	void updatePortfolio(Long budget, Long targetGain, Long maximumLoss) throws Exception {
		// given
		Member member = TestDataFactory.createMember();
		Portfolio portfolio = createPortfolio(
			member,
			"내꿈은 워렌버핏",
			Money.won(1000000L),
			Money.won(1500000L),
			Money.won(900000L)
		);
		PortfolioModifyResponse response = PortfolioModifyResponse.from(portfolio);

		BDDMockito.given(
				mockedPortfolioService.updatePortfolio(any(PortfolioModifyRequest.class), ArgumentMatchers.anyLong(),
					ArgumentMatchers.anyLong()))
			.willReturn(response);

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("name", "내꿈은 찰리몽거");
		requestBodyMap.put("securitiesFirm", "토스");
		requestBodyMap.put("budget", budget);
		requestBodyMap.put("targetGain", targetGain);
		requestBodyMap.put("maximumLoss", maximumLoss);

		String body = ObjectMapperUtil.serialize(requestBodyMap);
		// when & then
		mockMvc.perform(put("/api/portfolios/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오가 수정되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자는 포트폴리오 수정시 유효하지 않은 입력 정보로 추가할 수 없다")
	@MethodSource("co.fineants.TestDataProvider#invalidCreatePortfolioSource")
	@ParameterizedTest
	void updatePortfolioWithInvalidInput() throws Exception {
		// given
		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("name", "");
		requestBodyMap.put("securitiesFirm", "");
		requestBodyMap.put("budget", 0);
		requestBodyMap.put("targetGain", null);
		requestBodyMap.put("maximumLoss", -1);

		String body = ObjectMapperUtil.serialize(requestBodyMap);
		// when & then
		mockMvc.perform(put("/api/portfolios/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data").isArray());
	}

	@DisplayName("사용자는 포트폴리오 삭제를 요청한다")
	@Test
	void deletePortfolio() throws Exception {
		// given
		Portfolio portfolio = portfolioRepository.save(TestDataFactory.createPortfolio(member));
		// when & then
		mockMvc.perform(delete("/api/portfolios/{portfolioId}", portfolio.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(PortfolioSuccessCode.OK_DELETE_PORTFOLIO.getMessage())))
			.andExpect(jsonPath("data").value(nullValue()));
	}
}
