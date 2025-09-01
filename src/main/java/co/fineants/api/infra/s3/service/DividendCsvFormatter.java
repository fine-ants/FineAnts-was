package co.fineants.api.infra.s3.service;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;

@Component
public class DividendCsvFormatter {

	private static final String CSV_SEPARATOR = ",";
	private static final String CSV_HEADER = String.join(CSV_SEPARATOR, "id", "dividend", "recordDate", "paymentDate",
		"stockCode");

	public String format(StockDividend... dividends) {
		return String.join(System.lineSeparator(), CSV_HEADER, createCsvLines(dividends));
	}

	@NotNull
	private static String createCsvLines(StockDividend[] dividends) {
		return Arrays.stream(dividends)
			.map(StockDividend::toCsvLineString)
			.collect(Collectors.joining(Strings.LINE_SEPARATOR));
	}
}
