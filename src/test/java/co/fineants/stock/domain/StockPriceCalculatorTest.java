package co.fineants.stock.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;

class StockPriceCalculatorTest {

	private Money toWon(Expression expression) {
		return expression.reduce(Bank.getInstance(), Currency.KRW);
	}

	private Percentage toPercent(Expression dailyChangeRate) {
		return dailyChangeRate.toPercentage(Bank.getInstance(), Currency.KRW);
	}

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
	void calculateDailyChange_whenCurrentPriceIsGreaterThanClosingPrice_thenReturnPositiveDailyChange() {
		// given
		PriceCalculator calculator = new StockPriceCalculator();
		Money currentPrice = Money.won(1200);
		Money closingPrice = Money.won(1000);

		// when
		Expression dailyChange = calculator.calculateDailyChange(currentPrice, closingPrice);

		// then
		Assertions.assertThat(toWon(dailyChange)).isEqualTo(Money.won(200));
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
		Assertions.assertThat(toWon(dailyChange)).isEqualTo(Money.won(-200));
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
		Assertions.assertThat(toWon(dailyChange)).isEqualTo(Money.won(0));
	}

	@DisplayName("일일 변동액 계산 - 현재가에 null 값이 들어올 경우 예외가 발생한다.")
	@Test
	void calculateDailyChange_whenCurrentPriceIsNull_thenThrowException() {
		// given
		PriceCalculator calculator = new StockPriceCalculator();
		Money closingPrice = Money.won(1000);

		// when
		Throwable throwable = Assertions.catchThrowable(() -> calculator.calculateDailyChange(null, closingPrice));
		// then
		Assertions.assertThat(throwable)
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("일일 변동액 계산 - 종가에 null 값이 들어올 경우 예외가 발생한다.")
	@Test
	void calculateDailyChange_whenClosingPriceIsNull_thenThrowException() {
		// given
		PriceCalculator calculator = new StockPriceCalculator();
		Money currentPrice = Money.won(1000);

		// when
		Throwable throwable = Assertions.catchThrowable(() -> calculator.calculateDailyChange(currentPrice, null));
		// then
		Assertions.assertThat(throwable)
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("일일 변동액 비율 계산 - 일일 변동 금액 비율을 올바르게 계산한다.")
	@Test
	void calculateDailyChangeRate_whenCurrentPriceIsGraterThanClosingPrice_thenReturnPositiveDailyChangeRate() {
		// given
		PriceCalculator calculator = new StockPriceCalculator();
		Money currentPrice = Money.won(1200);
		Money closingPrice = Money.won(1000);

		// when
		Expression dailyChangeRate = calculator.calculateDailyChangeRate(currentPrice, closingPrice);

		// then
		Assertions.assertThat(toPercent(dailyChangeRate)).isEqualTo(Percentage.from(0.20));
	}

	@DisplayName("일일 변동액 비율 계산 - 음수 일일 변동 금액 비율을 올바르게 계산한다.")
	@Test
	void calculateDailyChangeRate_whenCurrentPriceIsLessThanClosingPrice_thenReturnNegativeDailyChangeRate() {
		// given
		PriceCalculator calculator = new StockPriceCalculator();
		Money currentPrice = Money.won(800);
		Money closingPrice = Money.won(1000);

		// when
		Expression dailyChangeRate = calculator.calculateDailyChangeRate(currentPrice, closingPrice);

		// then
		Assertions.assertThat(toPercent(dailyChangeRate)).isEqualTo(Percentage.from(-0.20));
	}

	@DisplayName("일일 변동액 비율 계산 - 현재가와 종가가 동일한 경우는 0 값으로 계산한다.")
	@Test
	void calculateDailyChangeRate_whenCurrentPriceIsEqualToClosingPrice_thenReturnZeroDailyChangeRate() {
		// given
		PriceCalculator calculator = new StockPriceCalculator();
		Money currentPrice = Money.won(1000);
		Money closingPrice = Money.won(1000);

		// when
		Expression dailyChangeRate = calculator.calculateDailyChangeRate(currentPrice, closingPrice);

		// then
		Assertions.assertThat(toPercent(dailyChangeRate)).isEqualTo(Percentage.from(0));
	}

	@DisplayName("일일 변동액 비율 계산 - 현재가에 null 값이 들어올 경우 예외가 발생한다.")
	@Test
	void calculateDailyChangeRate_whenCurrentPriceIsNull_thenThrowException() {
		// given
		PriceCalculator calculator = new StockPriceCalculator();
		Money closingPrice = Money.won(1000);

		// when
		Throwable throwable = Assertions.catchThrowable(() -> calculator.calculateDailyChangeRate(null, closingPrice));
		// then
		Assertions.assertThat(throwable)
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("일일 변동액 비율 계산 - 종가에 null 값이 들어올 경우 예외가 발생한다.")
	@Test
	void calculateDailyChangeRate_whenClosingPriceIsNull_thenThrowException() {
		// given
		PriceCalculator calculator = new StockPriceCalculator();
		Money currentPrice = Money.won(1000);

		// when
		Throwable throwable = Assertions.catchThrowable(() -> calculator.calculateDailyChangeRate(currentPrice, null));
		// then
		Assertions.assertThat(throwable)
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("일일 변동액 비율 계산 - 종가가 0원인 경우 0.0을 반환한다.")
	@Test
	void calculateDailyChangeRate_whenClosingPriceIsZero_thenReturnZero() {
		// given
		PriceCalculator calculator = new StockPriceCalculator();
		Money currentPrice = Money.won(1000);
		Money closingPrice = Money.won(0);

		// when
		Expression dailyChangeRate = calculator.calculateDailyChangeRate(currentPrice, closingPrice);

		// then
		Assertions.assertThat(toPercent(dailyChangeRate)).isEqualTo(Percentage.from(0.0));
	}
}
