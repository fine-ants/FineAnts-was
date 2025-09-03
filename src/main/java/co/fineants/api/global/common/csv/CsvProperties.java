package co.fineants.api.global.common.csv;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;

@Getter
@ConfigurationProperties(prefix = "csv")
public class CsvProperties {
	private CsvFormat stock;
	private CsvFormat stockDividend;

	public record CsvFormat(String delimiter, List<String> headers) {
	}
}
