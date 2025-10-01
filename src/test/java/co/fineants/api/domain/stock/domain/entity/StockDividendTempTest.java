package co.fineants.api.domain.stock.domain.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;

class StockDividendTempTest {
	@Test
	void calculateDividendSum() {
		StockDividendTemp stockDividendTemp = TestDataFactory.createSamsungStockDividendTemp();
		Count numShares = Count.from(10);

		Expression sum = stockDividendTemp.calculateDividendSum(numShares);

		Assertions.assertThat(sum).isEqualTo(Money.won(3610));
	}
}
