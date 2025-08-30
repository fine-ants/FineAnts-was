package co.fineants.api.infra.s3.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DividendCsvFormatterTest {

	private DividendCsvFormatter formatter;

	private String parseCsvHeader(String result) {
		return result.split("\n")[0];
	}

	@BeforeEach
	void setUp() {
		formatter = new DividendCsvFormatter();
	}

	@Test
	void canCreated() {
		Assertions.assertThat(formatter).isNotNull();
	}

	@Test
	void format_whenDataIsEmpty() {
		String result = formatter.format();

		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(parseCsvHeader(result))
			.contains("id,dividend,recordDate,paymentDate,stockCode");
	}
}
