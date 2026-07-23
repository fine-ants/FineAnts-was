package co.fineants.api.domain.holding.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.global.errors.exception.business.ForbiddenException;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockDividend;
import co.fineants.stock.domain.StockRepository;
import co.fineants.support.cache.PortfolioCacheSupportService;

class PortfolioHoldingServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private PortfolioHoldingService service;

	@Autowired
	private PurchaseHistoryRepository purchaseHistoryRepository;

	@Autowired
	private PortfolioHoldingRepository portfolioHoldingRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private PortfolioCacheSupportService portfolioCacheSupportService;

	@AfterEach
	void tearDown() {
		portfolioCacheSupportService.clear();
	}

	@DisplayName("회원은 다른 회원의 포트폴리오 종목들을 읽을 수 없다")
	@Test
	void readMyPortfolioStocks_whenReadOtherMemberHolding_thenThrowException() {
		// given
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(() -> service.readPortfolioHoldings(portfolio.getId()));
		// then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class)
			.hasMessage(portfolio.toString());
	}

	@DisplayName("회원은 다른 회원의 포트폴리오 차트를 조회할 수 없다")
	@Test
	void readMyPortfolioCharts_whenOtherMemberRead_thenThrowException() {
		// given
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		List<StockDividend> stockDividends = createStockDividendWith(stock.getTickerSymbol());
		stockDividends.forEach(stock::addStockDividend);
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(3);
		Money purchasePerShare = Money.won(50000);
		String memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo, portfolioHolding));
		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(
			() -> service.readPortfolioCharts(portfolio.getId()));
		// then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class)
			.hasMessage(portfolio.toString());
	}

	@DisplayName("사용자는 다수의 포트폴리오 삭제시 다른 회원의 포트폴리오 종목이 존재한다면 전부 삭제할 수 없다")
	@Test
	void deletePortfolioStocks_whenNotExistPortfolioHolding_thenError403() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock1 = stockRepository.save(createSamsungStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock1));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(3);
		Money purchasePerShare = Money.won(50000);
		String memo = "첫구매";
		PurchaseHistory purchaseHistory = purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo, portfolioHolding));

		Member member2 = memberRepository.save(createMember("일개미2222", "user2@gmail.com"));
		Portfolio portfolio2 = portfolioRepository.save(createPortfolio(member2));
		PortfolioHolding portfolioHolding2 = portfolioHoldingRepository.save(
			createPortfolioHolding(portfolio2, stock1));
		List<Long> portfolioHoldingIds = List.of(portfolioHolding.getId(), portfolioHolding2.getId());

		setAuthentication(member);
		// when
		Throwable throwable = catchThrowable(
			() -> service.deletePortfolioHoldings(portfolio.getId(), member.getId(), portfolioHoldingIds));

		// then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class)
			.hasMessage(portfolioHolding2.toString());
		assertThat(portfolioHoldingRepository.findById(portfolioHolding.getId())).isPresent();
		assertThat(portfolioHoldingRepository.findById(portfolioHolding2.getId())).isPresent();
		assertThat(purchaseHistoryRepository.findById(purchaseHistory.getId())).isPresent();
	}
}
