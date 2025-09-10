package co.fineants.api.domain.stock.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.stock.parser.StockCsvParser;
import co.fineants.api.domain.stock.parser.StockParser;

@Configuration
public class StockConfig {
	@Bean
	public StockCsvParser stockCsvParser(@Value("${csv.stock.delimiter}") String csvSeparator,
		StockParser stockParser) {
		return new StockCsvParser(csvSeparator, stockParser);
	}
}
