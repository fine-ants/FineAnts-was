package co.fineants.stock.domain.calculator;

import java.time.LocalDate;
import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.dividend.domain.entity.DividendDates;
import co.fineants.stock.domain.StockDividend;

class StockPriceCalculatorTest {

	private PriceCalculator calculator;

	private Money toWon(Expression expression) {
		return expression.reduce(Bank.getInstance(), Currency.KRW);
	}

	private Percentage toPercent(Expression dailyChangeRate) {
		return dailyChangeRate.toPercentage(Bank.getInstance(), Currency.KRW);
	}

	@NotNull
	private StockDividend createStockDividend(DividendDates dividendDates) {
		Money dividend = Money.won(1000);
		boolean isDeleted = false;
		String tickerSymbol = "005930";
		return new StockDividend(dividend, dividendDates, isDeleted, tickerSymbol);
	}

	@BeforeEach
	void setUp() {
		calculator = new StockPriceCalculator();
	}

	@DisplayName("일간 변동액 계산 - 일일 변동 금액을 올바르게 계산한다.")
	@Test
	void calculateDailyChange_whenCurrentPriceIsGreaterThanClosingPrice_thenReturnPositiveDailyChange() {
		// given
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
		Money currentPrice = Money.won(1000);
		Money closingPrice = Money.won(0);

		// when
		Expression dailyChangeRate = calculator.calculateDailyChangeRate(currentPrice, closingPrice);

		// then
		Assertions.assertThat(toPercent(dailyChangeRate)).isEqualTo(Percentage.from(0.0));
	}

	@DisplayName("연간 배당금 합계 계산 - 빈 배당금 리스트인 경우 0원을 반환한다.")
	@Test
	void calculateAnnualDividend_whenDividendsIsEmpty_thenReturnZero() {
		// given
		LocalDate baseDate = LocalDate.of(2023, 6, 1);

		// when
		Expression annualDividend = calculator.calculateAnnualDividend(
			Collections.emptyList(),
			baseDate
		);

		// then
		Assertions.assertThat(toWon(annualDividend)).isEqualTo(Money.zero());
	}

	@DisplayName("연간 배당금 합계 계산 - null 배당금 리스트인 경우 예외가 발생해야 한다")
	@Test
	void calculateAnnualDividend_whenDividendsIsNull_thenReturnZero() {
		// given
		LocalDate baseDate = LocalDate.of(2023, 6, 1);

		// when
		Throwable throwable = Assertions.catchThrowable(() -> calculator.calculateAnnualDividend(
			null,
			baseDate
		));

		// then
		Assertions.assertThat(throwable)
			.isInstanceOf(NullPointerException.class)
			.hasMessage("Stock dividends must not be null");
	}

	@DisplayName("연간 배당금 합계 계산 - 배당금 리스트의 원소가 1개인 경우 배당급 합계는 원소의 배당금 값과 같다.")
	@Test
	void calculateAnnualDividend_whenDividendsHasOneElement_thenReturnThatElementValue() {
		// given
		LocalDate recordDate = LocalDate.of(2023, 3, 31);
		LocalDate exDividendDate = LocalDate.of(2023, 4, 1);
		LocalDate paymentDate = LocalDate.of(2023, 5, 1);
		DividendDates dividendDates = DividendDates.of(recordDate, exDividendDate, paymentDate);
		StockDividend stockDividend = createStockDividend(dividendDates);

		LocalDate baseDate = LocalDate.of(2023, 6, 1);

		// when
		Expression annualDividend = calculator.calculateAnnualDividend(
			Collections.singletonList(stockDividend),
			baseDate
		);

		// then
		Assertions.assertThat(toWon(annualDividend)).isEqualTo(Money.won(1000));
	}

	@DisplayName("연간 배당금 합계 계산 - 배당금 리스트의 원소가 여러 개인 경우 올바른 배당금 합계를 반환한다.")
	@Test
	void calculateAnnualDividend_whenDividendsHasMultipleElements_thenReturnCorrectSum() {
		// given
		LocalDate recordDate1 = LocalDate.of(2023, 3, 31);
		LocalDate exDividendDate1 = LocalDate.of(2023, 4, 1);
		LocalDate paymentDate1 = LocalDate.of(2023, 5, 1);
		DividendDates dividendDates1 = DividendDates.of(recordDate1, exDividendDate1, paymentDate1);
		StockDividend stockDividend1 = createStockDividend(dividendDates1);

		LocalDate recordDate2 = LocalDate.of(2023, 6, 30);
		LocalDate exDividendDate2 = LocalDate.of(2023, 7, 1);
		LocalDate paymentDate2 = LocalDate.of(2023, 8, 1);
		DividendDates dividendDates2 = DividendDates.of(recordDate2, exDividendDate2, paymentDate2);
		StockDividend stockDividend2 = createStockDividend(dividendDates2);

		LocalDate baseDate = LocalDate.of(2023, 6, 1);

		// when
		Expression annualDividend = calculator.calculateAnnualDividend(
			java.util.List.of(stockDividend1, stockDividend2),
			baseDate
		);

		// then
		Assertions.assertThat(toWon(annualDividend)).isEqualTo(Money.won(2000));
	}

	@DisplayName("연간 배당금 합계 계산 - 배당금 지급일이 현재 연도가 아닌 경우 해당 배당금은 합계에 포함되지 않는다.")
	@Test
	void calculateAnnualDividend_whenDividendPaymentDateIsNotInCurrentYear_thenExcludeFromSum() {
		// given
		LocalDate recordDate1 = LocalDate.of(2022, 3, 31);
		LocalDate exDividendDate1 = LocalDate.of(2022, 4, 1);
		LocalDate paymentDate1 = LocalDate.of(2022, 6, 1);
		DividendDates dividendDates1 = DividendDates.of(recordDate1, exDividendDate1, paymentDate1);
		StockDividend stockDividend1 = createStockDividend(dividendDates1);

		LocalDate recordDate2 = LocalDate.of(2023, 6, 30);
		LocalDate exDividendDate2 = LocalDate.of(2023, 7, 1);
		LocalDate paymentDate2 = LocalDate.of(2023, 8, 1);
		DividendDates dividendDates2 = DividendDates.of(recordDate2, exDividendDate2, paymentDate2);
		StockDividend stockDividend2 = createStockDividend(dividendDates2);

		LocalDate baseDate = LocalDate.of(2023, 6, 1);

		// when
		Expression annualDividend = calculator.calculateAnnualDividend(
			java.util.List.of(stockDividend1, stockDividend2),
			baseDate
		);

		// then
		Assertions.assertThat(toWon(annualDividend)).isEqualTo(Money.won(1000));
	}
}
