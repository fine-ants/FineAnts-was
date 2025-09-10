package co.fineants.api.domain.stock.parser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import co.fineants.api.domain.stock.domain.entity.Stock;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StockCsvParser {

	private final String csvSeparator;
	private final StockParser stockParser;

	public StockCsvParser(String csvSeparator, StockParser stockParser) {
		this.csvSeparator = csvSeparator;
		this.stockParser = stockParser;
	}

	public List<Stock> parse(InputStream inputStream) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			return reader.lines()
				.skip(1) // skip header line
				.map(line -> line.split(csvSeparator))
				.map(stockParser::parse)
				.distinct()
				.toList();
		} catch (Exception e) {
			log.warn("Error reading stocks file", e);
			return Collections.emptyList();
		}
	}
}
