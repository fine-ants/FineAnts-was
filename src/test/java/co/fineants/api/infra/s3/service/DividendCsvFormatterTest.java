package co.fineants.api.infra.s3.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;

import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.stock.domain.entity.Market;
import co.fineants.api.domain.stock.domain.entity.Stock;

class DividendCsvFormatterTest {

	private DividendCsvFormatter formatter;

	private String parseCsvHeader(String result) {
		return result.split("\n")[0];
	}

	@BeforeEach
	void setUp() {
		formatter = new DividendCsvFormatter();
	}

	@Test
	void canCreated() {
		Assertions.assertThat(formatter).isNotNull();
	}

	@Test
	void format_whenDataIsEmpty() {
		String result = formatter.format();

		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(parseCsvHeader(result))
			.contains("id,dividend,recordDate,paymentDate,stockCode");
	}

	@Test
	void format_whenDataIsOne() throws IOException {
		StockDividend stockDividend = createSamsungStockDividend();
		String result = formatter.format(stockDividend);

		Assertions.assertThat(result).isNotNull();
		BufferedReader lead = new BufferedReader(new StringReader(result));
		BufferedReader gold = new BufferedReader(new StringReader("id,dividend,recordDate,paymentDate,stockCode"));
		String line;
		while ((line = gold.readLine()) != null) {
			String readLine = lead.readLine();
			Assertions.assertThat(readLine).isEqualTo(line);
		}
		Assertions.assertThat(lead.readLine()).isNull();
	}

	@NotNull
	private static StockDividend createSamsungStockDividend() {
		Long id = 1L;
		Money dividend = Money.won(361);
		LocalDate recordDate = LocalDate.of(2023, 3, 31);
		LocalDate exDividendDate = LocalDate.of(2023, 3, 30);
		LocalDate paymentDate = LocalDate.of(2024, 5, 17);
		Stock stock = Stock.of("005930", "삼성전자보통주", "SamsungElectronics", "KR7005930003", "전기전자", Market.KOSPI);
		return StockDividend.create(id, dividend, recordDate, exDividendDate, paymentDate,
			stock);
	}
}
