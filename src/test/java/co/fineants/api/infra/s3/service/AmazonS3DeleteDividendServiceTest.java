package co.fineants.api.infra.s3.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.amazonaws.services.s3.AmazonS3;

class AmazonS3DeleteDividendServiceTest {

	private DeleteDividendService service;

	@BeforeEach
	void setUp() {
		String bucketName = "fineants2024";
		String dividendPath = "local/dividend/dividends.csv";
		AmazonS3 amazonS3 = Mockito.mock(AmazonS3.class);
		service = new AmazonS3DeleteDividendService(bucketName, dividendPath, amazonS3);
	}

	@Test
	void canCreated() {
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void delete() {
		Assertions.assertThatCode(service::delete).doesNotThrowAnyException();
	}
}
