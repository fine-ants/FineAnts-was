package co.fineants.stock.domain.calculator;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.dividend.domain.entity.DividendDates;
import co.fineants.stock.domain.StockDividend;

class StockDividendCalculatorTest {

	@DisplayName("객체 생성 테스트")
	@Test
	void canCreated() {
		// when
		DividendCalculator calculator = new StockDividendCalculator();

		// then
		Assertions.assertThat(calculator).isNotNull();
	}

	@DisplayName("배당 월 리스트 계산 - 매개변수가 null이면 예외를 던진다")
	@Test
	void calculateDividendMonths_ThrowsException_WhenDividendsIsNull() {
		// given
		DividendCalculator calculator = new StockDividendCalculator();
		List<StockDividend> dividends = Collections.emptyList();
		LocalDate baseDate = LocalDate.of(2023, 6, 1);
		// when & then
		Assertions.assertThatThrownBy(() -> calculator.calculateDividendMonths(null, baseDate))
			.isInstanceOf(NullPointerException.class);
		Assertions.assertThatThrownBy(() -> calculator.calculateDividendMonths(dividends, null))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("배당 월 리스트 계산 - 배당금 리스트가 비어있으면 빈 리스트를 반환한다")
	@Test
	void calculateDividendMonths_ReturnsEmptyList_WhenDividendsIsEmpty() {
		// given
		DividendCalculator calculator = new StockDividendCalculator();
		List<StockDividend> dividends = Collections.emptyList();
		LocalDate baseDate = LocalDate.of(2023, 6, 1);
		// when
		List<Integer> actual = calculator.calculateDividendMonths(dividends, baseDate);
		// then
		Assertions.assertThat(actual).isEmpty();
	}

	@DisplayName("배당 월 리스트 계산 - 배당 리스트 원소가 한개 있으면 해당 월 리스트를 반환한다")
	@Test
	void calculateDividendMonths_ReturnsMonthList_WhenDividendsHasOneElement() {
		// given
		DividendCalculator calculator = new StockDividendCalculator();
		LocalDate recordDate = LocalDate.of(2023, 3, 31);
		LocalDate exDividendDate = LocalDate.of(2023, 4, 1);
		LocalDate paymentDate = LocalDate.of(2023, 5, 1);
		DividendDates dividendDates = DividendDates.of(recordDate, exDividendDate, paymentDate);

		StockDividend dividend = TestDataFactory.createStockDividend(dividendDates);
		List<StockDividend> dividends = List.of(dividend);
		LocalDate baseDate = LocalDate.of(2023, 6, 1);
		// when
		List<Integer> actual = calculator.calculateDividendMonths(dividends, baseDate);
		// then
		List<Integer> expected = List.of(5);
		Assertions.assertThat(actual).isEqualTo(expected);
	}

	@DisplayName("배당 월 리스트 계산 - 배당 리스트 원소가 여러개 있으면 해당 월 리스트를 반환한다")
	@Test
	void calculateDividendMonths_ReturnsMonthList_WhenDividendsHasMultipleElements() {
		// given
		DividendCalculator calculator = new StockDividendCalculator();

		DividendDates dividendDates1 = DividendDates.of(
			LocalDate.of(2023, 3, 31),
			LocalDate.of(2023, 4, 1),
			LocalDate.of(2023, 5, 1)
		);
		DividendDates dividendDates2 = DividendDates.of(
			LocalDate.of(2023, 6, 30),
			LocalDate.of(2023, 7, 1),
			LocalDate.of(2023, 8, 1)
		);
		DividendDates dividendDates3 = DividendDates.of(
			LocalDate.of(2022, 9, 30),
			LocalDate.of(2022, 10, 1),
			LocalDate.of(2022, 11, 1)
		);

		StockDividend dividend1 = TestDataFactory.createStockDividend(dividendDates1);
		StockDividend dividend2 = TestDataFactory.createStockDividend(dividendDates2);
		StockDividend dividend3 = TestDataFactory.createStockDividend(dividendDates3);

		List<StockDividend> dividends = List.of(dividend1, dividend2, dividend3);
		LocalDate baseDate = LocalDate.of(2023, 6, 1);
		// when
		List<Integer> actual = calculator.calculateDividendMonths(dividends, baseDate);
		// then
		List<Integer> expected = List.of(5, 8);
		Assertions.assertThat(actual).isEqualTo(expected);
	}
}
