package co.fineants.api.infra.s3.service;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;

public class DividendCsvFormatter {

	private static final String CSV_SEPARATOR = ",";

	public String format(StockDividend... dividends) {
		return String.join(System.lineSeparator(), createHeader(), createLines(dividends));
	}

	@NotNull
	private static String createLines(StockDividend[] dividends) {
		return Arrays.stream(dividends)
			.map(StockDividend::toCsvLineString)
			.collect(Collectors.joining(Strings.LINE_SEPARATOR));
	}

	@NotNull
	private static String createHeader() {
		return String.join(CSV_SEPARATOR, "id", "dividend", "recordDate", "paymentDate", "stockCode");
	}
}
