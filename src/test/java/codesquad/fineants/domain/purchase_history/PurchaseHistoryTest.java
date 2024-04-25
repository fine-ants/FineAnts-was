package codesquad.fineants.domain.purchase_history;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Money;

@ActiveProfiles("test")
class PurchaseHistoryTest {

	@DisplayName("매입 내역의 투자 금액을 구한다")
	@Test
	void calculateInvestmentAmount() {
		// given
		PurchaseHistory purchaseHistory = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(5L))
			.purchasePricePerShare(Money.won(10000.0))
			.build();

		// when
		Money result = purchaseHistory.calculateInvestmentAmount();

		// then
		assertThat(result).isEqualByComparingTo(Money.won(50000L));
	}

}
