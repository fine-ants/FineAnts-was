package co.fineants.stock.domain.calculator;

import java.time.LocalDate;
import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.dividend.domain.entity.DividendDates;
import co.fineants.api.global.common.time.DefaultLocalDateTimeService;
import co.fineants.api.global.common.time.LocalDateTimeService;
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
		LocalDateTimeService service = new DefaultLocalDateTimeService();
		// when
		Expression annualDividend = calculator.calculateAnnualDividend(
			Collections.emptyList(),
			service
		);

		// then
		Assertions.assertThat(toWon(annualDividend)).isEqualTo(Money.zero());
	}

	@DisplayName("연간 배당금 합계 계산 - null 배당금 리스트인 경우 0원을 반환한다.")
	@Test
	void calculateAnnualDividend_whenDividendsIsNull_thenReturnZero() {
		// given
		LocalDateTimeService service = new DefaultLocalDateTimeService();
		// when
		Expression annualDividend = calculator.calculateAnnualDividend(
			null,
			service
		);

		// then
		Assertions.assertThat(toWon(annualDividend)).isEqualTo(Money.zero());
	}

	@DisplayName("연간 배당금 합계 계산 - 배당금 리스트의 원소가 1개인 경우 배당급 합계는 원소의 배당금 값과 같다.")
	@Test
	void calculateAnnualDividend_whenDividendsHasOneElement_thenReturnThatElementValue() {
		// given
		LocalDateTimeService localDateTimeService = Mockito.mock(LocalDateTimeService.class);
		BDDMockito.given(localDateTimeService.getLocalDateWithNow())
			.willReturn(LocalDate.of(2023, 6, 1));
		LocalDate recordDate = LocalDate.of(2023, 3, 31);
		LocalDate exDividendDate = LocalDate.of(2023, 4, 1);
		LocalDate paymentDate = LocalDate.of(2023, 5, 1);
		DividendDates dividendDates = DividendDates.of(recordDate, exDividendDate, paymentDate);
		StockDividend stockDividend = createStockDividend(dividendDates);
		// when
		Expression annualDividend = calculator.calculateAnnualDividend(
			Collections.singletonList(stockDividend),
			localDateTimeService
		);

		// then
		Assertions.assertThat(toWon(annualDividend)).isEqualTo(Money.won(1000));
	}
}
