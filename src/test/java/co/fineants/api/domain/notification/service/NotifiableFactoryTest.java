package co.fineants.api.domain.notification.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Predicate;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.common.notification.PortfolioMaximumLossNotifiable;
import co.fineants.api.domain.common.notification.PortfolioTargetGainNotifiable;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.kis.service.CurrentPriceService;
import co.fineants.api.domain.notification.domain.entity.type.NotificationType;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.stock_target_price.repository.StockTargetPriceRepository;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.NotificationPreference;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.calculator.DividendCalculator;
import co.fineants.stock.domain.calculator.StockDividendCalculator;

@ExtendWith(MockitoExtension.class)
class NotifiableFactoryTest {

	@InjectMocks
	private NotifiableFactory factory;

	@Mock
	private PortfolioRepository portfolioRepository;

	@Mock
	private StockTargetPriceRepository stockTargetPriceRepository;

	@Mock
	private CurrentPriceService currentPriceService;
	private PortfolioCalculator portfolioCalculator;

	@BeforeEach
	void setUp() {
		LocalDateTimeService timeService = BDDMockito.mock(LocalDateTimeService.class);
		DividendCalculator dividendCalculator = new StockDividendCalculator();
		portfolioCalculator = new PortfolioCalculator(currentPriceService, timeService, dividendCalculator);
	}

	@DisplayName("포트폴리오 목표 수익 금액 알림 데이터 생성")
	@Test
	void should_return_portfolio_notification_data_when_notification_type_is_target_gain() {
		// given
		Member member = TestDataFactory.createMember(1L);
		Portfolio portfolio = TestDataFactory.createPortfolio(1L, member);
		Stock stock = TestDataFactory.createSamsungStock();
		PortfolioHolding holding = TestDataFactory.createPortfolioHolding(1L, portfolio, stock);

		LocalDateTime now = LocalDate.of(2026, 7, 30).atStartOfDay();
		Count numShares = Count.from(75);
		Money purchasePerShare = Money.won(30_000L);
		String memo = "첫구매";
		PurchaseHistory history = TestDataFactory.createPurchaseHistory(1L, now, numShares, purchasePerShare, memo,
			holding);

		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		BDDMockito.given(currentPriceService.fetchPrice(stock.getTickerSymbol()))
			.willReturn(Money.won(50_000L));
		BDDMockito.given(portfolioRepository.findByPortfolioIdWithAll(portfolio.getId()))
			.willReturn(Optional.of(portfolio));
		Predicate<Portfolio> reachedPredicate = portfolioCalculator::reachedTargetGainBy;
		// when
		Notifiable notifiable = factory.getPortfolio(portfolio.getId(), reachedPredicate);
		// then
		PortfolioTargetGainNotifiable expected = PortfolioTargetGainNotifiable.builder()
			.title("포트폴리오")
			.content("내꿈은 워렌버핏의 목표 수익률을 달성했습니다")
			.type(NotificationType.PORTFOLIO_TARGET_GAIN)
			.referenceId(portfolio.getReferenceId())
			.memberId(member.getId())
			.link(portfolio.getLink())
			.name(portfolio.name())
			.preference(NotificationPreference.allActive())
			.isReached(true)
			.isActive(true)
			.id(portfolio.getId())
			.build();
		Assertions.assertThat(notifiable).isEqualTo(expected);
	}

	@DisplayName("포트폴리오 최대 손실 금액 알림 데이터 생성")
	@Test
	void should_return_portfolio_notification_data_when_notification_type_is_maximum_loss() {
		// given
		Member member = TestDataFactory.createMember(1L);
		Portfolio portfolio = TestDataFactory.createPortfolio(1L, member);
		Stock stock = TestDataFactory.createSamsungStock();
		PortfolioHolding holding = TestDataFactory.createPortfolioHolding(1L, portfolio, stock);

		LocalDateTime now = LocalDate.of(2026, 7, 30).atStartOfDay();
		Count numShares = Count.from(75);
		Money purchasePerShare = Money.won(30_000L);
		String memo = "첫구매";
		PurchaseHistory history = TestDataFactory.createPurchaseHistory(1L, now, numShares, purchasePerShare, memo,
			holding);

		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		BDDMockito.given(currentPriceService.fetchPrice(stock.getTickerSymbol()))
			.willReturn(Money.won(20_000L));
		BDDMockito.given(portfolioRepository.findByPortfolioIdWithAll(portfolio.getId()))
			.willReturn(Optional.of(portfolio));
		Predicate<Portfolio> reachedPredicate = portfolioCalculator::reachedMaximumLossBy;
		// when
		Notifiable notifiable = factory.getPortfolio(portfolio.getId(), reachedPredicate);
		// then
		Notifiable expected = PortfolioMaximumLossNotifiable.builder()
			.title("포트폴리오")
			.content(String.format("%s이(가) 최대 손실율에 도달했습니다", portfolio.name()))
			.type(NotificationType.PORTFOLIO_MAX_LOSS)
			.referenceId(portfolio.getReferenceId())
			.memberId(member.getId())
			.link(portfolio.getLink())
			.name(portfolio.name())
			.preference(NotificationPreference.allActive())
			.isReached(true)
			.isActive(true)
			.id(portfolio.getId())
			.build();
		Assertions.assertThat(notifiable)
			.isInstanceOf(PortfolioMaximumLossNotifiable.class)
			.isEqualTo(expected);
	}
}
