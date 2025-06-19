package co.fineants.api.domain.holding.service;

import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.dto.request.PortfolioHoldingCreateRequest;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.domain.dto.request.PurchaseHistoryCreateRequest;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.errors.exception.business.ForbiddenException;

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
		Throwable throwable = Assertions.catchThrowable(
			() -> portfolioHoldingFacade.createPortfolioHolding(request, portfolio.getId()));
		// then
		Assertions.assertThat(throwable)
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
		Assertions.assertThat(portfolioHolding).isNotNull();
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
		Assertions.assertThat(portfolioHolding).isNotNull();
		Assertions.assertThat(purchaseHistoryRepository.findAllByPortfolioHoldingId(portfolioHolding.getId()))
			.hasSize(1);
	}
}
