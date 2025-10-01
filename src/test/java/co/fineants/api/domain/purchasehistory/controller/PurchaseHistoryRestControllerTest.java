package co.fineants.api.domain.purchasehistory.controller;

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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.domain.dto.request.PurchaseHistoryCreateRequest;
import co.fineants.api.domain.purchasehistory.domain.dto.response.PurchaseHistoryCreateResponse;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.purchasehistory.service.PurchaseHistoryService;
import co.fineants.api.global.errors.exception.business.CashNotSufficientInvalidInputException;
import co.fineants.api.global.util.ObjectMapperUtil;
import co.fineants.support.controller.ControllerTestSupport;

class PurchaseHistoryRestControllerTest extends ControllerTestSupport {

	@Autowired
	private PurchaseHistoryService mockedPurchaseHistoryService;

	@Autowired
	private PortfolioRepository mockedPortfolioRepository;

	@Override
	protected Object initController() {
		return new PurchaseHistoryRestController(mockedPurchaseHistoryService);
	}

	@DisplayName("사용자가 매입 이력을 추가한다")
	@CsvSource(value = {"3", "10000000000000000000000000000"})
	@ParameterizedTest
	void addPurchaseHistory(Count numShares) throws Exception {
		// given
		Long memberId = 1L;
		Member member = TestDataFactory.createMember();
		Portfolio portfolio = createPortfolio(member);
		PortfolioHolding portfolioHolding = createPortfolioHolding(portfolio, createSamsungStock());
		PurchaseHistory purchaseHistory = createPurchaseHistory(1L, LocalDateTime.of(2023, 10, 23, 10, 0, 0),
			Count.from(3), Money.won(50000), "memo", portfolioHolding);
		String url = String.format("/api/portfolio/%d/holdings/%d/purchaseHistory", portfolio.getId(),
			portfolioHolding.getId());
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("purchaseDate", LocalDateTime.now().toString());
		requestBody.put("numShares", numShares.getValue());
		requestBody.put("purchasePricePerShare", 50000);
		requestBody.put("memo", "첫구매");

		given(mockedPurchaseHistoryService.createPurchaseHistory(
			ArgumentMatchers.any(PurchaseHistoryCreateRequest.class),
			anyLong(),
			anyLong(),
			anyLong()
		)).willReturn(
			PurchaseHistoryCreateResponse.from(purchaseHistory, portfolio.getId(), memberId)
		);
		given(mockedPortfolioRepository.findById(anyLong())).willReturn(Optional.of(portfolio));

		// when & then
		mockMvc.perform(post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(ObjectMapperUtil.serialize(requestBody)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo("매입 이력이 추가되었습니다")))
			.andExpect(jsonPath("data.id").value(equalTo(purchaseHistory.getId().intValue())))
			.andExpect(jsonPath("data.portfolioId").value(equalTo(portfolio.getId().intValue())))
			.andExpect(jsonPath("data.memberId").value(equalTo(memberId.intValue())));
	}

	@DisplayName("사용자가 매입 이력 추가시 유효하지 않은 입력으로 추가할 수 없다")
	@Test
	void addPurchaseHistoryWithInvalidInput() throws Exception {
		// given
		Portfolio portfolio = createPortfolio(TestDataFactory.createMember());
		PortfolioHolding portfolioHolding = createPortfolioHolding(portfolio, createSamsungStock());
		String url = String.format("/api/portfolio/%d/holdings/%d/purchaseHistory", portfolio.getId(),
			portfolioHolding.getId());
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("purchaseDate", null);
		requestBody.put("numShares", 0);
		requestBody.put("purchasePricePerShare", 0);
		requestBody.put("memo", "첫구매");

		given(mockedPortfolioRepository.findById(anyLong()))
			.willReturn(Optional.of(portfolio));

		// when & then
		mockMvc.perform(post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(ObjectMapperUtil.serialize(requestBody)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data").isArray());
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
