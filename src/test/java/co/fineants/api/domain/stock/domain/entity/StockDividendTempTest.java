package co.fineants.api.domain.stock.domain.entity;

import java.time.LocalDate;

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

	@Test
	void calculateDividendSum_whenNumSharesIsZero_thenReturnZero() {
		StockDividendTemp stockDividendTemp = TestDataFactory.createSamsungStockDividendTemp();
		Count numShares = Count.zero();

		Expression sum = stockDividendTemp.calculateDividendSum(numShares);

		Assertions.assertThat(sum).isEqualTo(Money.won(0));
	}

	@Test
	void isCurrentMonthPaymentDate() {
		StockDividendTemp stockDividendTemp = TestDataFactory.createSamsungStockDividendTemp();
		LocalDate today = LocalDate.of(2023, 5, 1);

		boolean actual = stockDividendTemp.isCurrentMonthPaymentDate(today);

		Assertions.assertThat(actual).isTrue();
	}
}
