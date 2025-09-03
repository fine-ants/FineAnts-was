package co.fineants.api.infra.s3.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class StockCsvFormatterTest {

	@Test
	void canCreated() {
		StockCsvFormatter formatter = new StockCsvFormatter();

		Assertions.assertThat(formatter).isNotNull();
	}

	@Test
	void format_whenStockIsEmpty() {
		StockCsvFormatter formatter = new StockCsvFormatter();

		String content = formatter.format();

		Assertions.assertThat(content).isEqualTo("stockCode$tickerSymbol$companyName$companyNameEng$sector$market");
	}
}
