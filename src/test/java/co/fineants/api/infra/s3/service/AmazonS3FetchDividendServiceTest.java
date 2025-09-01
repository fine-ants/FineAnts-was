package co.fineants.api.infra.s3.service;

import java.io.File;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.amazonaws.services.s3.AmazonS3;

class AmazonS3FetchDividendServiceTest {
	@Test
	void canCreated() {
		String bucketName = "fineants2024";
		String dividendPath = "local/dividend/dividends.csv";
		AmazonS3 amazonS3 = Mockito.mock(AmazonS3.class);
		FetchDividendService service = new AmazonS3FetchDividendService(bucketName, dividendPath, amazonS3);
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void fetchDividend() {
		String bucketName = "fineants2024";
		String dividendPath = "local/dividend/dividends.csv";
		AmazonS3 amazonS3 = Mockito.mock(AmazonS3.class);
		FetchDividendService service = new AmazonS3FetchDividendService(bucketName, dividendPath, amazonS3);

		File file = service.fetchDividend();

		Assertions.assertThat(file).isNotNull();
	}
}
