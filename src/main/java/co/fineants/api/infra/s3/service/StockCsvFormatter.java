package co.fineants.api.infra.s3.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import co.fineants.api.domain.stock.domain.entity.Stock;

@Component
public class StockCsvFormatter {
	private static final String CSV_DELIMITER = "$";
	private static final String CSV_HEADER = String.join(CSV_DELIMITER,
		"stockCode",
		"tickerSymbol",
		"companyName",
		"companyNameEng",
		"sector",
		"market"
	);

	public String format(Stock... stocks) {
		String lines = createLines(Arrays.asList(stocks));
		return String.join(Strings.LINE_SEPARATOR, CSV_HEADER, lines).trim();
	}

	@NotNull
	private String createLines(List<Stock> stocks) {
		return stocks.stream()
			.map(Stock::toCsvLineString)
			.collect(Collectors.joining(Strings.LINE_SEPARATOR));
	}
}
