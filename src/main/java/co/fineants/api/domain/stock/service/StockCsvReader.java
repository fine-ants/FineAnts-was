package co.fineants.api.domain.stock.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import co.fineants.stock.domain.Market;
import co.fineants.stock.domain.Stock;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StockCsvReader {

	public static final String CSV_DELIMITER = "$";

	private final String tickerSymbolPrefix;

	public StockCsvReader(@Value("${csv.stock.tickerSymbolPrefix}") String tickerSymbolPrefix) {
		this.tickerSymbolPrefix = tickerSymbolPrefix;
	}

	public Set<Stock> readStockCsv() {
		Resource resource = new ClassPathResource("stocks.csv");

		Set<Stock> result = new HashSet<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
			Iterable<CSVRecord> records = CSVFormat.Builder.create()
				.setHeader("stockCode", "tickerSymbol", "companyName", "companyNameEng", "sector", "market")
				.setSkipHeaderRecord(true)
				.setDelimiter(CSV_DELIMITER)
				.build()
				.parse(reader);

			for (CSVRecord csvRecord : records) {
				Stock stock = Stock.of(
					csvRecord.get("tickerSymbol").replace(tickerSymbolPrefix, Strings.EMPTY),
					csvRecord.get("companyName"),
					csvRecord.get("companyNameEng"),
					csvRecord.get("stockCode"),
					csvRecord.get("sector"),
					Market.ofMarket(csvRecord.get("market"))
				);
				result.add(stock);
			}
		} catch (IOException e) {
			return Collections.emptySet();
		}
		return result;
	}
}
