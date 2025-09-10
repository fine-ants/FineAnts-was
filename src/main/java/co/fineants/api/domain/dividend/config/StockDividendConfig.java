package co.fineants.api.domain.dividend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.dividend.domain.calculator.ExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.calculator.MySqlExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.parser.StockDividendCsvLineParser;
import co.fineants.api.domain.dividend.domain.parser.StockDividendCsvParser;
import co.fineants.api.domain.holiday.service.HolidayService;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class StockDividendConfig {

	private final HolidayService service;

	@Bean
	public ExDividendDateCalculator exDividendDateCalculator() {
		return new MySqlExDividendDateCalculator(service);
	}

	@Bean
	public StockDividendCsvParser stockDividendCsvParser(@Value("${csv.stockDividend.delimiter}") String csvSeparator,
		StockDividendCsvLineParser lineParser) {
		return new StockDividendCsvParser(csvSeparator, lineParser);
	}
}
