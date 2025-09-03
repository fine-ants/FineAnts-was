package co.fineants.api.infra.s3.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

import co.fineants.api.domain.stock.domain.entity.Stock;

public class StockCsvFormatter {
	private static final String CSV_DELIMITER = "$";

	public String format(Stock... stocks) {
		String title = csvTitle();
		String lines = csvLines(Arrays.asList(stocks));
		return String.join(Strings.LINE_SEPARATOR, title, lines).trim();
	}

	@NotNull
	private String csvTitle() {
		return String.join(CSV_DELIMITER, "stockCode", "tickerSymbol", "companyName", "companyNameEng",
			"sector", "market");
	}

	@NotNull
	private String csvLines(List<Stock> stocks) {
		return stocks.stream()
			.map(Stock::toCsvLineString)
			.collect(Collectors.joining(Strings.LINE_SEPARATOR));
	}
}
