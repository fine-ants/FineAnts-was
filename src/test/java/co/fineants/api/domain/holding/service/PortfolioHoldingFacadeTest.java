package co.fineants.api.domain.holding.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.dto.request.PortfolioHoldingCreateRequest;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.domain.dto.request.PurchaseHistoryCreateRequest;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.errors.exception.business.ForbiddenException;
import co.fineants.api.global.errors.exception.business.StockNotFoundException;
import co.fineants.member.domain.Member;
import co.fineants.member.infrastructure.MemberRepository;

class PortfolioHoldingFacadeTest extends AbstractContainerBaseTest {

	@Autowired
	private PortfolioHoldingFacade portfolioHoldingFacade;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private PurchaseHistoryRepository purchaseHistoryRepository;

	@Autowired
	private PortfolioHoldingRepository portfolioHoldingRepository;

	@DisplayName("사용자는 다른 사용자의 포트폴리오를 대상으로 포트폴리오 종목을 추가할 수 없다")
	@Test
	void createPortfolioHolding_whenHackerCreateOtherMemberPortfolioHolding_thenThrowException() {
		// given
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		setAuthentication(hacker);
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));

		PortfolioHoldingCreateRequest request = PortfolioHoldingCreateRequest.create(
			"005930",
			PurchaseHistoryCreateRequest.create(
				LocalDateTime.now(),
				Count.from(3),
				Money.won(50_000),
				"memo"
			)
		);
		// when
		Throwable throwable = catchThrowable(
			() -> portfolioHoldingFacade.createPortfolioHolding(request, portfolio.getId()));
		// then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class);
	}

	@DisplayName("포트폴리오 종목 생성 시 매입 이력 생성 요청이 null인 경우, 포트폴리오 종목만 저장한다")
	@Test
	void createPortfolioHolding_whenOnlyPortfolioHolding_thenSavePortfolioHolding() {
		// given
		Member member = memberRepository.save(createMember());
		setAuthentication(member);
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock samsung = stockRepository.save(createSamsungStock());

		PortfolioHoldingCreateRequest request = PortfolioHoldingCreateRequest.create(samsung.getTickerSymbol(), null);
		// when
		PortfolioHolding portfolioHolding = portfolioHoldingFacade.createPortfolioHolding(request, portfolio.getId());

		// then
		assertThat(portfolioHolding).isNotNull();
	}

	@DisplayName("포트폴리오 종목 생성 시 매입 이력 생성 요청이 null이 아닌 경우, 포트폴리오 종목과 매입 이력을 저장한다")
	@Test
	void createPortfolioHolding_whenPurchaseHistoryCreateRequestIsNotNull_thenSavePortfolioHoldingAndPurchaseHistory() {
		// given
		Member member = memberRepository.save(createMember());
		setAuthentication(member);
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock samsung = stockRepository.save(createSamsungStock());

		PurchaseHistoryCreateRequest purchaseHistoryCreateRequest = PurchaseHistoryCreateRequest.create(
			LocalDateTime.now(),
			Count.from(3),
			Money.won(50_000),
			"memo"
		);
		PortfolioHoldingCreateRequest request = PortfolioHoldingCreateRequest.create(samsung.getTickerSymbol(),
			purchaseHistoryCreateRequest);
		// when
		PortfolioHolding portfolioHolding = portfolioHoldingFacade.createPortfolioHolding(request, portfolio.getId());

		// then
		assertThat(portfolioHolding).isNotNull();
		assertThat(purchaseHistoryRepository.findAllByPortfolioHoldingId(portfolioHolding.getId()))
			.hasSize(1);
	}

	@DisplayName("기존 포트폴리오 종목이 있는 상태에서 매입 이력과 같이 포트폴리오 종목을 같이 생성 요청 시, 매입 이력을 추가한다")
	@Test
	void createPortfolioHolding_whenExistPortfolioHolding_thenSavePurchaseHistory() {
		Member member = memberRepository.save(createMember());
		setAuthentication(member);
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock samsung = stockRepository.save(createSamsungStock());
		portfolioHoldingRepository.save(PortfolioHolding.of(portfolio, samsung));

		PurchaseHistoryCreateRequest purchaseHistoryCreateRequest = PurchaseHistoryCreateRequest.create(
			LocalDateTime.now(),
			Count.from(3),
			Money.won(50_000),
			"memo"
		);
		PortfolioHoldingCreateRequest request = PortfolioHoldingCreateRequest.create(samsung.getTickerSymbol(),
			purchaseHistoryCreateRequest);
		// when
		PortfolioHolding portfolioHolding = portfolioHoldingFacade.createPortfolioHolding(request, portfolio.getId());

		// then
		assertThat(portfolioHolding).isNotNull();
		assertThat(portfolioHoldingRepository.findAllByPortfolio(portfolio)).hasSize(1);
		assertThat(purchaseHistoryRepository.findAllByPortfolioHoldingId(portfolioHolding.getId()))
			.hasSize(1);
	}

	@DisplayName("포트폴리오 종목과 매입 이력 추가시 매입 이력 필수 입력 정보를 넣지 않으면 포트폴리오 종목만 추가된다")
	@Test
	void createPortfolioHolding_whenInvalidPurchaseHistory_thenSaveOnlyPortfolioHolding() {
		Member member = memberRepository.save(createMember());
		setAuthentication(member);
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock samsung = stockRepository.save(createSamsungStock());

		PurchaseHistoryCreateRequest purchaseHistoryCreateRequest = PurchaseHistoryCreateRequest.create(
			null,
			null,
			null,
			null
		);
		PortfolioHoldingCreateRequest request = PortfolioHoldingCreateRequest.create(samsung.getTickerSymbol(),
			purchaseHistoryCreateRequest);
		// when
		PortfolioHolding portfolioHolding = portfolioHoldingFacade.createPortfolioHolding(request, portfolio.getId());

		// then
		assertThat(portfolioHolding).isNotNull();
		assertThat(portfolioHoldingRepository.findAllByPortfolio(portfolio)).hasSize(1);
		assertThat(purchaseHistoryRepository.findAllByPortfolioHoldingId(portfolioHolding.getId()))
			.isEmpty();
	}

	@DisplayName("포트폴리오 종목 추가할 때 존재하지 않는 종목인 경우에는 추가할 수 없다")
	@Test
	void whenTickerSymbolIsNotFound_thenThrowException() {
		Member member = memberRepository.save(createMember());
		setAuthentication(member);
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));

		PurchaseHistoryCreateRequest purchaseHistoryCreateRequest = PurchaseHistoryCreateRequest.create(
			LocalDateTime.now(),
			Count.from(3),
			Money.won(50_000),
			"memo"
		);
		String invalidTickerSymbol = "INVALID_TICKER";
		PortfolioHoldingCreateRequest request = PortfolioHoldingCreateRequest.create(invalidTickerSymbol,
			purchaseHistoryCreateRequest);
		// when
		Throwable throwable = catchThrowable(
			() -> portfolioHoldingFacade.createPortfolioHolding(request, portfolio.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(StockNotFoundException.class)
			.hasMessage(invalidTickerSymbol);
	}
}
