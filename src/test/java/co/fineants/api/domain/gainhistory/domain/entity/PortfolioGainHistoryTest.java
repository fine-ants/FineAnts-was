package co.fineants.api.domain.gainhistory.domain.entity;

import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.stock.domain.entity.Stock;

class PortfolioGainHistoryTest extends AbstractContainerBaseTest {

	@DisplayName("빈 히스토리 상태에서 새로운 손익내역을 생성한다")
	@Test
	void test() {
		// given
		Member member = createMember();
		Portfolio portfolio = createPortfolio(member);
		Stock stock = createSamsungStock();
		Stock stock2 = createDongwhaPharmStock();
		PortfolioHolding holding1 = PortfolioHolding.of(portfolio, stock, Money.won(20000L));
		PortfolioHolding holding2 = PortfolioHolding.of(portfolio, stock2, Money.won(20000L));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(5);
		Money purchasePerShare = Money.won(10000);
		String memo = "첫구매";
		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo,
			holding1);

		numShares = Count.from(5);
		purchasePerShare = Money.won(20000);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo,
			holding2);

		holding1.addPurchaseHistory(purchaseHistory1);
		holding2.addPurchaseHistory(purchaseHistory2);

		portfolio.addHolding(holding1);
		portfolio.addHolding(holding2);

		PortfolioGainHistory history = PortfolioGainHistory.empty(portfolio);
		PortfolioCalculator calculator = new PortfolioCalculator();
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
