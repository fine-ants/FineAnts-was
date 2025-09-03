package co.fineants.api.global.common.csv;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;

@Getter
@ConfigurationProperties(prefix = "csv")
public class CsvProperties {
	private final CsvFormat stock;
	private final CsvFormat stockDividend;

	public CsvProperties(CsvFormat stock, CsvFormat stockDividend) {
		this.stock = stock;
		this.stockDividend = stockDividend;
	}

	public record CsvFormat(String delimiter, List<String> headers) {
	}
}
