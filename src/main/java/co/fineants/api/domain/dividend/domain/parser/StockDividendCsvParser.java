package co.fineants.api.domain.dividend.domain.parser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import co.fineants.api.domain.stock.domain.entity.StockDividend;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StockDividendCsvParser {

	private final String csvSeparator;
	private final StockDividendCsvLineParser stockDividendCsvLineParser;

	public StockDividendCsvParser(String csvSeparator, StockDividendCsvLineParser stockDividendCsvLineParser) {
		this.csvSeparator = csvSeparator;
		this.stockDividendCsvLineParser = stockDividendCsvLineParser;
	}

	public List<StockDividend> parse(InputStream inputStream) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			return reader.lines()
				.skip(1) // Skip header line
				.map(line -> line.split(csvSeparator))
				.map(stockDividendCsvLineParser::parseCsvLine)
				.distinct()
				.toList();
		} catch (Exception e) {
			log.error("Failed to parse dividend file from Remote Storage", e);
			return Collections.emptyList();
		}
	}
}
