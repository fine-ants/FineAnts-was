package co.fineants.api.infra.s3.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class DividendCsvFormatterTest {

	@Test
	void canCreated() {
		DividendCsvFormatter formatter = new DividendCsvFormatter();
		Assertions.assertThat(formatter).isNotNull();
	}

	@Test
	void format() {
		DividendCsvFormatter formatter = new DividendCsvFormatter();

		String result = formatter.format();

		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(result.split("\n")[0])
			.contains("id,dividend,recordDate,paymentDate,stockCode");
	}
}
