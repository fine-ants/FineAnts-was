package co.fineants.api.infra.s3.service.imple;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.dividend.domain.parser.StockDividendParser;
import co.fineants.api.domain.stock.domain.entity.Stock;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StockDividendCsvParser {

	private final String csvSeparator;
	private final StockDividendParser stockDividendParser;

	public StockDividendCsvParser(String csvSeparator, StockDividendParser stockDividendParser) {
		this.csvSeparator = csvSeparator;
		this.stockDividendParser = stockDividendParser;
	}

	public List<StockDividend> parse(InputStream inputStream, Map<String, Stock> stockMap) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			return reader.lines()
				.skip(1) // Skip header line
				.map(line -> line.split(csvSeparator))
				.map(columns -> stockDividendParser.parseCsvLine(columns,
					stockMap))
				.filter(dividend -> dividend.getStock() != null)
				.distinct()
				.toList();
		} catch (Exception e) {
			log.error("Failed to parse dividend file from Google Storage", e);
			return Collections.emptyList();
		}
	}
}
