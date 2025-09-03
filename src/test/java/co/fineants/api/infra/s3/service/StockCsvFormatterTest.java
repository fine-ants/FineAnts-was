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
	void format_whenStockIsMultiple() {
		StockCsvFormatter formatter = new StockCsvFormatter();
		Stock stock1 = TestDataFactory.createSamsungStock();
		Stock stock2 = TestDataFactory.createDongwhaPharmStock();

		String content = formatter.format(stock1, stock2);

		new FileContentComparator().compare(content, "src/test/resources/gold_stocks.csv");
	}
}
