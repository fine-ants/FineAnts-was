package co.fineants.api.infra.s3.service;

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
	private FileContentComparator fileContentComparator;

	@NotNull
	private StockDividend createSamsungStockDividend() {
		Long id = 1L;
		Money dividend = Money.won(361);
		LocalDate recordDate = LocalDate.of(2023, 3, 31);
		LocalDate exDividendDate = LocalDate.of(2023, 3, 30);
		LocalDate paymentDate = LocalDate.of(2023, 5, 17);
		Stock stock = Stock.of("005930", "삼성전자보통주", "SamsungElectronics", "KR7005930003", "전기전자", Market.KOSPI);
		return StockDividend.create(id, dividend, recordDate, exDividendDate, paymentDate,
			stock);
	}

	@NotNull
	private StockDividend createKakaoStockDividend() {
		Long id = 2L;
		Money dividend = Money.won(68);
		LocalDate recordDate = LocalDate.of(2025, 3, 10);
		LocalDate exDividendDate = LocalDate.of(2025, 3, 7);
		LocalDate paymentDate = LocalDate.of(2025, 4, 24);
		Stock stock = Stock.of("035720", "카카오보통주", "Kakao", "KR7035720002", "서비스업", Market.KOSPI);
		return StockDividend.create(id, dividend, recordDate, exDividendDate, paymentDate,
			stock);
	}

	@BeforeEach
	void setUp() {
		formatter = new DividendCsvFormatter();
		fileContentComparator = new FileContentComparator();
	}

	@Test
	void canCreated() {
		Assertions.assertThat(formatter).isNotNull();
	}

	@Test
	void format_whenDataIsEmpty() {
		String content = formatter.format();

		Assertions.assertThat(content).isNotNull();
		fileContentComparator.compare(content, "src/test/resources/gold_empty_dividends.csv");
	}

	@Test
	void format_whenDataIsOne() {
		StockDividend stockDividend = createSamsungStockDividend();
		String content = formatter.format(stockDividend);

		Assertions.assertThat(content).isNotNull();
		fileContentComparator.compare(content, "src/test/resources/gold_dividends.csv");
	}

	@Test
	void format_whenDateIsTwo() {
		StockDividend stockDividend1 = createSamsungStockDividend();
		StockDividend stockDividend2 = createKakaoStockDividend();
		String content = formatter.format(stockDividend1, stockDividend2);

		Assertions.assertThat(content).isNotNull();
		fileContentComparator.compare(content, "src/test/resources/gold_dividends_2.csv");
	}
}
