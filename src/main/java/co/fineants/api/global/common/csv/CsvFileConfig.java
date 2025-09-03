package co.fineants.api.global.common.csv;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.infra.s3.service.CsvFormatter;

@Configuration
public class CsvFileConfig {

	private final CsvProperties csvProperties;

	public CsvFileConfig(CsvProperties csvProperties) {
		this.csvProperties = csvProperties;
	}

	@Bean
	public CsvFormatter<Stock> stockCsvFormatter() {
		CsvProperties.CsvFormat csvFormat = csvProperties.getStock();
		String delimiter = csvFormat.delimiter();
		List<String> headers = csvFormat.headers();
		return new CsvFormatter<>(delimiter, headers);
	}

	@Bean
	public CsvFormatter<StockDividend> stockDividendCsvFormatter() {
		CsvProperties.CsvFormat csvFormat = csvProperties.getStockDividend();
		String delimiter = csvFormat.delimiter();
		List<String> headers = csvFormat.headers();
		return new CsvFormatter<>(delimiter, headers);
	}
}
