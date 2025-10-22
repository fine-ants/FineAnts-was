package co.fineants.api.domain.stock.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import co.fineants.stock.domain.Stock;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StockCsvParser {

	private final String csvSeparator;
	private final StockCsvLineParser stockCsvLineParser;

	public StockCsvParser(@Value("${csv.stock.delimiter}") String csvSeparator, StockCsvLineParser stockCsvLineParser) {
		this.csvSeparator = csvSeparator;
		this.stockCsvLineParser = stockCsvLineParser;
	}

	public List<Stock> parse(InputStream inputStream) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			return reader.lines()
				.skip(1) // skip header line
				.map(line -> line.split(csvSeparator))
				.map(stockCsvLineParser::parse)
				.distinct()
				.toList();
		} catch (Exception e) {
			log.warn("Error reading stocks file", e);
			return Collections.emptyList();
		}
	}
}
