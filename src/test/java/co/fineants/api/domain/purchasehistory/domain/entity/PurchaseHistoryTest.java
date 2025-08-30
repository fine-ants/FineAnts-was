package co.fineants.api.domain.purchasehistory.domain.entity;

import static co.fineants.api.domain.common.money.Currency.*;
import static org.assertj.core.api.Assertions.*;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;

import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;

class PurchaseHistoryTest {

	@DisplayName("매입 내역의 투자 금액을 구한다")
	@Test
	void calculateInvestmentAmount() {
		// given
		PortfolioHolding holding = BDDMockito.mock(PortfolioHolding.class);
		PurchaseHistory purchaseHistory = PurchaseHistory.now(
			Money.won(10000),
			Count.from(5),
			Strings.EMPTY,
			holding
		);
		Bank bank = Bank.getInstance();
		// when
		Expression result = purchaseHistory.calInvestmentAmount();

		// then
		Money actual = bank.reduce(result, KRW);
		assertThat(actual).isEqualByComparingTo(Money.won(50000L));
	}

}
