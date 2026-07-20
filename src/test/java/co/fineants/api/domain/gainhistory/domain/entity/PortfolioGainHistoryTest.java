package co.fineants.api.domain.gainhistory.domain.entity;

import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.member.domain.Member;
import co.fineants.stock.domain.Stock;

@ExtendWith(MockitoExtension.class)
class PortfolioGainHistoryTest {
	@Mock
	private PortfolioCalculator calculator;

	@DisplayName("빈 히스토리 상태에서 새로운 손익내역을 생성한다")
	@Test
	void createNewHistory() {
		// given
		Member member = TestDataFactory.createMember();
		Portfolio portfolio = TestDataFactory.createPortfolio(member);
		Stock stock = TestDataFactory.createSamsungStock();
		Stock stock2 = TestDataFactory.createDongwhaPharmStock();
		PortfolioHolding holding1 = PortfolioHolding.of(portfolio, stock);
		PortfolioHolding holding2 = PortfolioHolding.of(portfolio, stock2);

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(5);
		Money purchasePerShare = Money.won(10000);
		String memo = "첫구매";
		PurchaseHistory purchaseHistory1 = TestDataFactory.createPurchaseHistory(1L, purchaseDate, numShares,
			purchasePerShare, memo,
			holding1);

		numShares = Count.from(5);
		purchasePerShare = Money.won(20000);
		PurchaseHistory purchaseHistory2 = TestDataFactory.createPurchaseHistory(2L, purchaseDate, numShares,
			purchasePerShare, memo,
			holding2);

		holding1.addPurchaseHistory(purchaseHistory1);
		holding2.addPurchaseHistory(purchaseHistory2);

		portfolio.addHolding(holding1);
		portfolio.addHolding(holding2);

		PortfolioGainHistory history = PortfolioGainHistory.empty(portfolio);

		BDDMockito.given(calculator.calTotalGainBy(portfolio))
			.willReturn(Money.won(50_000L));
		BDDMockito.given(calculator.calDailyGain(history, portfolio))
			.willReturn(Money.won(50_000L));
		BDDMockito.given(calculator.calBalanceBy(portfolio))
			.willReturn(Money.won(850_000L));
		BDDMockito.given(calculator.calTotalCurrentValuationBy(portfolio))
			.willReturn(Money.won(200_000L));
		// when
		PortfolioGainHistory actual = history.createNewHistory(calculator);
		// then
		Money totalGain = Money.won(50_000L);
		Money dailyGain = Money.won(50_000L);
		Money cash = Money.won(850_000L);
		Money totalCurrentValuation = Money.won(200_000L);
		PortfolioGainHistory expected = PortfolioGainHistory.create(totalGain, dailyGain, cash, totalCurrentValuation,
			portfolio);
		Assertions.assertThat(actual).isEqualTo(expected);
	}

}
