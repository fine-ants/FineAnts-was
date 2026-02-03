package co.fineants.stock.domain.calculator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockDividendCalculatorTest {

	@DisplayName("객체 생성 테스트")
	@Test
	void canCreated() {
		// when
		DividendCalculator calculator = new StockDividendCalculator();

		// then
		Assertions.assertThat(calculator).isNotNull();
	}
}
