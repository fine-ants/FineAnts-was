package co.fineants.stock.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;

class StockPriceCalculatorTest {

	@DisplayName("객체 생성 - 객체가 정상적으로 생성됩니다.")
	@Test
	void canCreated() {
		// when
		PriceCalculator calculator = new StockPriceCalculator();

		// then
		Assertions.assertThat(calculator).isNotNull();
	}

	@DisplayName("일간 변동액 계산 - 일일 변동 금액을 올바르게 계산한다.")
	@Test
	void calculateDailyChange_whenCurrentPriceIsGraterThanClosingPrice_thenReturnPositiveDailyChange() {
		// given
		PriceCalculator calculator = new StockPriceCalculator();
		Money currentPrice = Money.won(1200);
		Money closingPrice = Money.won(1000);

		// when
		Expression dailyChange = calculator.calculateDailyChange(currentPrice, closingPrice);

		// then
		Assertions.assertThat(dailyChange).isEqualTo(Money.won(200));
	}

	@DisplayName("일일 변동액 계산 - 음수 일일 변동 금액을 올바르게 계산한다.")
	@Test
	void calculateDailyChange_whenCurrentPriceIsLessThanClosingPrice_thenReturnNegativeDailyChange() {
		// given
		PriceCalculator calculator = new StockPriceCalculator();
		Money currentPrice = Money.won(800);
		Money closingPrice = Money.won(1000);

		// when
		Expression dailyChange = calculator.calculateDailyChange(currentPrice, closingPrice);

		// then
		Assertions.assertThat(dailyChange).isEqualTo(Money.won(-200));
	}

	@DisplayName("일일 변동액 계산 - 현재가와 종가가 동일한 경우는 0 값으로 계산한다.")
	@Test
	void calculateDailyChange_whenCurrentPriceIsEqualToClosingPrice_thenReturnZeroDailyChange() {
		// given
		PriceCalculator calculator = new StockPriceCalculator();
		Money currentPrice = Money.won(1000);
		Money closingPrice = Money.won(1000);

		// when
		Expression dailyChange = calculator.calculateDailyChange(currentPrice, closingPrice);

		// then
		Assertions.assertThat(dailyChange).isEqualTo(Money.won(0));
	}
}
