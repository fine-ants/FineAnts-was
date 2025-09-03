package co.fineants.api.infra.s3.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.stock.domain.entity.Stock;

class CsvFormatterTest {

	private CsvFormatter<Stock> formatter;

	@BeforeEach
	void setUp() {
		String delimiter = "$";
		String[] headers = {"stockCode", "tickerSymbol", "companyName", "companyNameEng", "sector", "market"};
		formatter = new CsvFormatter<>(delimiter, headers);
	}

	@Test
	void canCreated() {
		Assertions.assertThat(formatter).isNotNull();
	}

	@Test
	void format_whenStockIsEmpty() {
		String content = formatter.format();

		Assertions.assertThat(content).isEqualTo("stockCode$tickerSymbol$companyName$companyNameEng$sector$market");
	}

	@Test
	void format_whenStockIsMultiple() {
		Stock stock1 = TestDataFactory.createSamsungStock();
		Stock stock2 = TestDataFactory.createDongwhaPharmStock();

		String content = formatter.format(stock1, stock2);

		Assertions.assertThat(content).isNotNull();
		new FileContentComparator().compare(content, "src/test/resources/gold_stocks.csv");
	}
}
