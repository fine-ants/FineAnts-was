package co.fineants.stock.domain.calculator;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
