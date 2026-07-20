package co.fineants.api.domain.holding.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.dto.request.PortfolioHoldingCreateRequest;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.service.PortfolioService;
import co.fineants.api.domain.purchasehistory.domain.dto.request.PurchaseHistoryCreateRequest;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.purchasehistory.service.PurchaseHistoryService;
import co.fineants.api.global.errors.exception.business.StockNotFoundException;
import co.fineants.member.domain.Member;
import co.fineants.stock.application.FindStock;
import co.fineants.stock.domain.Stock;

@ExtendWith(MockitoExtension.class)
class PortfolioHoldingFacadeUnitTest {
	@Mock
	private PortfolioService portfolioService;

	@Mock
	private FindStock findStock;

	@Mock
	private PortfolioHoldingService portfolioHoldingService;

	@Mock
	private PurchaseHistoryService purchaseHistoryService;

	@InjectMocks
	private PortfolioHoldingFacade portfolioHoldingFacade;

	@DisplayName("포트폴리오 종목 생성 시 매입 이력 생성 요청이 null인 경우, 포트폴리오 종목만 저장한다")
	@Test
	void createPortfolioHolding_whenOnlyPortfolioHolding_thenSavePortfolioHolding() {
		// given
		Member member = TestDataFactory.createMember(1L);
		Portfolio portfolio = TestDataFactory.createPortfolio(1L, member);
		Stock samsung = TestDataFactory.createSamsungStock();

		PortfolioHoldingCreateRequest request = PortfolioHoldingCreateRequest.create(samsung.getTickerSymbol(), null);
		BDDMockito.given(portfolioService.findPortfolio(portfolio.getId()))
			.willReturn(portfolio);
		BDDMockito.given(findStock.byTickerSymbol(request.getTickerSymbol()))
			.willReturn(samsung);
		BDDMockito.given(portfolioHoldingService.getPortfolioHoldingBy(portfolio, samsung))
			.willReturn(Optional.empty());
		PortfolioHolding holding = PortfolioHolding.of(1L, portfolio, samsung);
		BDDMockito.given(portfolioHoldingService.savePortfolioHolding(holding))
			.willReturn(holding);

		// when
		PortfolioHolding portfolioHolding = portfolioHoldingFacade.createPortfolioHolding(request, portfolio.getId());

		// then
		assertThat(portfolioHolding).isEqualTo(holding);
		BDDMockito.verifyNoInteractions(purchaseHistoryService);
	}

	@DisplayName("포트폴리오 종목 생성 시 매입 이력 생성 요청이 null이 아닌 경우, 포트폴리오 종목과 매입 이력을 저장한다")
	@Test
	void createPortfolioHolding_whenPurchaseHistoryCreateRequestIsNotNull_thenSavePortfolioHoldingAndPurchaseHistory() {
		// given
		Member member = TestDataFactory.createMember(1L);
		Portfolio portfolio = TestDataFactory.createPortfolio(1L, member);
		Stock samsung = TestDataFactory.createSamsungStock();

		LocalDateTime purchaseDate = LocalDate.of(2026, 7, 20).atStartOfDay();
		Count numShares = Count.from(3);
		Money purchasePricePerShare = Money.won(50_000);
		String memo = "memo";
		PurchaseHistoryCreateRequest purchaseHistoryCreateRequest = PurchaseHistoryCreateRequest.create(
			purchaseDate,
			numShares,
			purchasePricePerShare,
			memo
		);
		PortfolioHoldingCreateRequest request = PortfolioHoldingCreateRequest.create(samsung.getTickerSymbol(),
			purchaseHistoryCreateRequest);
		BDDMockito.given(portfolioService.findPortfolio(portfolio.getId()))
			.willReturn(portfolio);
		BDDMockito.given(findStock.byTickerSymbol(request.getTickerSymbol()))
			.willReturn(samsung);
		BDDMockito.given(portfolioHoldingService.getPortfolioHoldingBy(portfolio, samsung))
			.willReturn(Optional.empty());
		PortfolioHolding holding = PortfolioHolding.of(1L, portfolio, samsung);
		BDDMockito.given(portfolioHoldingService.savePortfolioHolding(holding))
			.willReturn(holding);

		// when
		PortfolioHolding portfolioHolding = portfolioHoldingFacade.createPortfolioHolding(request, portfolio.getId());

		// then
		assertThat(portfolioHolding).isNotNull();
		PurchaseHistory purchaseHistory = PurchaseHistory.create(purchaseDate, numShares, purchasePricePerShare, memo,
			holding);
		BDDMockito.verify(purchaseHistoryService, Mockito.times(1))
			.savePurchaseHistory(purchaseHistory, portfolio);
	}

	@DisplayName("기존 포트폴리오 종목이 있는 상태에서 매입 이력과 같이 포트폴리오 종목을 같이 생성 요청 시, 매입 이력을 추가한다")
	@Test
	void createPortfolioHolding_whenExistPortfolioHolding_thenSavePurchaseHistory() {
		Member member = TestDataFactory.createMember(1L);
		Portfolio portfolio = TestDataFactory.createPortfolio(1L, member);
		Stock samsung = TestDataFactory.createSamsungStock();

		LocalDateTime purchaseDate = LocalDate.of(2026, 7, 20).atStartOfDay();
		Count numShares = Count.from(3);
		Money purchasePricePerShare = Money.won(50_000);
		String memo = "memo";
		PurchaseHistoryCreateRequest purchaseHistoryCreateRequest = PurchaseHistoryCreateRequest.create(
			purchaseDate,
			numShares,
			purchasePricePerShare,
			memo
		);
		PortfolioHolding holding = PortfolioHolding.of(1L, portfolio, samsung);

		PortfolioHoldingCreateRequest request = PortfolioHoldingCreateRequest.create(samsung.getTickerSymbol(),
			purchaseHistoryCreateRequest);
		BDDMockito.given(portfolioService.findPortfolio(portfolio.getId()))
			.willReturn(portfolio);
		BDDMockito.given(findStock.byTickerSymbol(samsung.getTickerSymbol()))
			.willReturn(samsung);
		BDDMockito.given(portfolioHoldingService.getPortfolioHoldingBy(portfolio, samsung))
			.willReturn(Optional.of(holding));
		BDDMockito.given(portfolioHoldingService.savePortfolioHolding(holding))
			.willReturn(holding);
		// when
		PortfolioHolding portfolioHolding = portfolioHoldingFacade.createPortfolioHolding(request, portfolio.getId());
		// then
		assertThat(portfolioHolding).isEqualTo(holding);
		PurchaseHistory purchaseHistory = PurchaseHistory.create(purchaseDate, numShares, purchasePricePerShare, memo,
			holding);
		BDDMockito.verify(purchaseHistoryService, Mockito.times(1))
			.savePurchaseHistory(purchaseHistory, portfolio);
	}

	@DisplayName("포트폴리오 종목과 매입 이력 추가시 매입 이력 필수 입력 정보를 넣지 않으면 포트폴리오 종목만 추가된다")
	@Test
	void createPortfolioHolding_whenInvalidPurchaseHistory_thenSaveOnlyPortfolioHolding() {
		Member member = TestDataFactory.createMember(1L);
		Portfolio portfolio = TestDataFactory.createPortfolio(1L, member);
		Stock samsung = TestDataFactory.createSamsungStock();

		PurchaseHistoryCreateRequest purchaseHistoryCreateRequest = PurchaseHistoryCreateRequest.create(
			null,
			null,
			null,
			null
		);
		PortfolioHoldingCreateRequest request = PortfolioHoldingCreateRequest.create(samsung.getTickerSymbol(),
			purchaseHistoryCreateRequest);
		BDDMockito.given(portfolioService.findPortfolio(portfolio.getId()))
			.willReturn(portfolio);
		BDDMockito.given(findStock.byTickerSymbol(samsung.getTickerSymbol()))
			.willReturn(samsung);
		BDDMockito.given(portfolioHoldingService.getPortfolioHoldingBy(portfolio, samsung))
			.willReturn(Optional.empty());
		PortfolioHolding holding = PortfolioHolding.of(portfolio, samsung);
		PortfolioHolding saveHolding = PortfolioHolding.of(1L, portfolio, samsung);
		BDDMockito.given(portfolioHoldingService.savePortfolioHolding(holding))
			.willReturn(saveHolding);
		// when
		PortfolioHolding portfolioHolding = portfolioHoldingFacade.createPortfolioHolding(request, portfolio.getId());

		// then
		assertThat(portfolioHolding).isEqualTo(saveHolding);
		BDDMockito.verifyNoInteractions(purchaseHistoryService);
	}

	@DisplayName("포트폴리오 종목 추가할 때 존재하지 않는 종목인 경우에는 추가할 수 없다")
	@Test
	void whenTickerSymbolIsNotFound_thenThrowException() {
		Member member = TestDataFactory.createMember(1L);
		Portfolio portfolio = TestDataFactory.createPortfolio(1L, member);

		PurchaseHistoryCreateRequest purchaseHistoryCreateRequest = PurchaseHistoryCreateRequest.create(
			LocalDateTime.now(),
			Count.from(3),
			Money.won(50_000),
			"memo"
		);
		String invalidTickerSymbol = "INVALID_TICKER";
		PortfolioHoldingCreateRequest request = PortfolioHoldingCreateRequest.create(invalidTickerSymbol,
			purchaseHistoryCreateRequest);
		BDDMockito.given(portfolioService.findPortfolio(portfolio.getId()))
			.willReturn(portfolio);
		BDDMockito.given(findStock.byTickerSymbol(invalidTickerSymbol))
			.willThrow(new StockNotFoundException(invalidTickerSymbol));
		// when
		Throwable throwable = catchThrowable(
			() -> portfolioHoldingFacade.createPortfolioHolding(request, portfolio.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(StockNotFoundException.class)
			.hasMessage(invalidTickerSymbol);
	}
}
