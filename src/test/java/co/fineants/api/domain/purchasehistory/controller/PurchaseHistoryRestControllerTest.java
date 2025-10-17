package co.fineants.api.domain.purchasehistory.controller;

import static co.fineants.api.global.success.PurchaseHistorySuccessCode.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.anyLong;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.domain.dto.request.PurchaseHistoryCreateRequest;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.purchasehistory.service.PurchaseHistoryService;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.errors.exception.business.CashNotSufficientInvalidInputException;
import co.fineants.api.global.util.ObjectMapperUtil;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;

class PurchaseHistoryRestControllerTest extends AbstractContainerBaseTest {

	@Autowired
	private PurchaseHistoryService mockedPurchaseHistoryService;

	@Autowired
	private PortfolioRepository mockedPortfolioRepository;

	@Autowired
	private PurchaseHistoryRestController controller;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private PortfolioHoldingRepository portfolioHoldingRepository;

	@Autowired
	private StockRepository stockRepository;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = createMockMvc(controller);
	}

	@DisplayName("사용자가 매입 이력을 추가한다")
	@Test
	void createPurchaseHistory() throws Exception {
		// given
		Member member = memberRepository.save(TestDataFactory.createMember());
		Portfolio portfolio = portfolioRepository.save(TestDataFactory.createPortfolio(member));
		Stock stock = TestDataFactory.createSamsungStock();
		TestDataFactory.createSamsungStockDividends().forEach(stock::addStockDividend);
		Stock saveStock = stockRepository.save(stock);

		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(
			createPortfolioHolding(portfolio, saveStock));

		PurchaseHistoryCreateRequest request = new PurchaseHistoryCreateRequest(
			LocalDateTime.now(),
			Count.from(3),
			Money.won(50000),
			"첫구매"
		);

		// when & then
		mockMvc.perform(
				post("/api/portfolio/{portfolioId}/holdings/{portfolioHoldingId}/purchaseHistory", portfolio.getId(),
					portfolioHolding.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.CREATED.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.CREATED.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(CREATED_ADD_PURCHASE_HISTORY.getMessage())))
			.andExpect(jsonPath("data.id").value(greaterThan(0)))
			.andExpect(jsonPath("data.portfolioId").value(equalTo(portfolio.getId().intValue())))
			.andExpect(jsonPath("data.memberId").value(equalTo(member.getId().intValue())));
	}

	@DisplayName("사용자가 매입 이력 추가시 유효하지 않은 입력으로 추가할 수 없다")
	@Test
	void createPurchaseHistory_whenInvalidPurchaseHistory_thenNotSavePurchaseHistory() throws Exception {
		// given
		Member member = memberRepository.save(TestDataFactory.createMember());
		Portfolio portfolio = portfolioRepository.save(TestDataFactory.createPortfolio(member));
		Stock stock = TestDataFactory.createSamsungStock();
		TestDataFactory.createSamsungStockDividends().forEach(stock::addStockDividend);
		Stock saveStock = stockRepository.save(stock);
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(
			createPortfolioHolding(portfolio, saveStock));

		PurchaseHistoryCreateRequest request = new PurchaseHistoryCreateRequest(
			null,
			Count.from(0),
			Money.won(0),
			"첫구매"
		);

		// when & then
		mockMvc.perform(
				post("/api/portfolio/{portfolioId}/holdings/{portfolioHoldingId}/purchaseHistory", portfolio.getId(),
					portfolioHolding.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.BAD_REQUEST.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.BAD_REQUEST.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data").isArray())
			.andExpect(jsonPath("data[*].field",
				containsInAnyOrder("purchaseDate", "numShares", "purchasePricePerShare")))
			.andExpect(jsonPath("data[*].defaultMessage", containsInAnyOrder(
				"매입날짜는 날짜 형식의 필수 정보입니다",
				"개수는 양수여야 합니다",
				"금액은 양수여야 합니다"
			)));
	}

	@DisplayName("사용자가 매입 이력 추가시 현금이 부족해 실패한다")
	@Test
	void addPurchaseHistoryThrowsExceptionWhenTotalInvestmentExceedsBudget() throws Exception {
		// given
		Portfolio portfolio = createPortfolio(TestDataFactory.createMember());
		PortfolioHolding portfolioHolding = createPortfolioHolding(portfolio, createSamsungStock());
		String url = String.format("/api/portfolio/%d/holdings/%d/purchaseHistory", portfolio.getId(),
			portfolioHolding.getId());
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("purchaseDate", LocalDateTime.now().toString());
		requestBody.put("numShares", 3);
		requestBody.put("purchasePricePerShare", 50000);
		requestBody.put("memo", "첫구매");

		String body = ObjectMapperUtil.serialize(requestBody);

		given(mockedPortfolioRepository.findById(anyLong())).willReturn(Optional.of(portfolio));

		given(mockedPurchaseHistoryService.createPurchaseHistory(
			any(PurchaseHistoryCreateRequest.class),
			anyLong(),
			anyLong(),
			anyLong())).willThrow(new CashNotSufficientInvalidInputException(Money.won(150_000).toString()));

		// when & then
		mockMvc.perform(post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(body))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("Cash Not Sufficient For Purchase")))
			.andExpect(jsonPath("data").value(equalTo(Money.won(150_000).toString())));
	}

	@DisplayName("사용자가 매입 이력을 수정한다")
	@Test
	void modifyPurchaseHistory() throws Exception {
		// given
		Portfolio portfolio = createPortfolio(TestDataFactory.createMember());
		PortfolioHolding portfolioHolding = createPortfolioHolding(portfolio, createSamsungStock());
		PurchaseHistory purchaseHistory = createPurchaseHistory(1L, LocalDateTime.now(), Count.from(3),
			Money.won(50000), "첫구매", portfolioHolding);
		String url = String.format("/api/portfolio/%d/holdings/%d/purchaseHistory/%d", portfolio.getId(),
			portfolioHolding.getId(), purchaseHistory.getId());
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("purchaseDate", LocalDateTime.now().toString());
		requestBody.put("numShares", 4);
		requestBody.put("purchasePricePerShare", 50000);
		requestBody.put("memo", "첫구매");

		String body = ObjectMapperUtil.serialize(requestBody);

		given(mockedPortfolioRepository.findById(anyLong())).willReturn(Optional.of(portfolio));

		// when & then
		mockMvc.perform(put(url)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("매입 이력이 수정되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자가 매입 이력을 삭제한다")
	@Test
	void deletePurchaseHistory() throws Exception {
		// given
		Portfolio portfolio = createPortfolio(TestDataFactory.createMember());
		PortfolioHolding portfolioHolding = createPortfolioHolding(portfolio, createSamsungStock());
		PurchaseHistory purchaseHistory = createPurchaseHistory(1L, LocalDateTime.now(), Count.from(3),
			Money.won(50000), "첫구매", portfolioHolding);
		String url = String.format("/api/portfolio/%d/holdings/%d/purchaseHistory/%d", portfolio.getId(),
			portfolioHolding.getId(), purchaseHistory.getId());

		given(mockedPortfolioRepository.findById(anyLong())).willReturn(Optional.of(portfolio));

		// when & then
		mockMvc.perform(delete(url))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("매입 이력이 삭제되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}
}
