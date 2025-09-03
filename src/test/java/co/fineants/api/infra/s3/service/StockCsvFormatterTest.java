package co.fineants.api.infra.s3.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.stock.domain.entity.Stock;

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

	@Test
	void format_whenStockIsOne() {
		StockCsvFormatter formatter = new StockCsvFormatter();
		Stock stock = TestDataFactory.createSamsungStock();

		String content = formatter.format(stock);

		new FileContentComparator().compare(content, "src/test/resources/gold_stocks.csv");
	}
}
