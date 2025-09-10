package co.fineants.api.domain.stock.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.stock.parser.StockCsvLineParser;
import co.fineants.api.domain.stock.parser.StockCsvParser;

@Configuration
public class StockConfig {
	@Bean
	public StockCsvParser stockCsvParser(@Value("${csv.stock.delimiter}") String csvSeparator,
		StockCsvLineParser stockCsvLineParser) {
		return new StockCsvParser(csvSeparator, stockCsvLineParser);
	}
}
