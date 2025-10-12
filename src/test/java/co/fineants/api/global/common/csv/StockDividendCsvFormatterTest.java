package co.fineants.api.global.common.csv;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.stock.domain.entity.StockDividend;
import co.fineants.api.infra.s3.service.imple.FileContentComparator;

class StockDividendCsvFormatterTest {

	private CsvFormatter<StockDividend> formatter;

	@BeforeEach
	void setUp() {
		formatter = new CsvFormatter<>(",",
			new String[] {"tickerSymbol", "dividend", "recordDate", "paymentDate", "isDeleted"});
	}

	@Test
	void format_whenStockDividendIsEmpty() {
		String content = formatter.format();

		Assertions.assertThat(content).isNotNull();
		FileContentComparator.compare(content, "src/test/resources/gold_empty_dividends.csv");
	}

	@Test
	void format_whenDataIsOne() {
		StockDividend stockDividend = TestDataFactory.createSamsungStockDividend();
		String content = formatter.format(stockDividend);

		Assertions.assertThat(content).isNotNull();
		FileContentComparator.compare(content, "src/test/resources/gold_dividends.csv");
	}

	@Test
	void format_whenDateIsTwo() {
		StockDividend stockDividend1 = TestDataFactory.createSamsungStockDividend();
		StockDividend stockDividend2 = TestDataFactory.createKakaoStockDividend();
		String content = formatter.format(stockDividend1, stockDividend2);

		Assertions.assertThat(content).isNotNull();
		FileContentComparator.compare(content, "src/test/resources/gold_dividends_2.csv");
	}
}
