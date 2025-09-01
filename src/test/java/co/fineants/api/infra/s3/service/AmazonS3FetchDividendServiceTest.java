package co.fineants.api.infra.s3.service;

import java.io.File;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.amazonaws.services.s3.AmazonS3;

class AmazonS3FetchDividendServiceTest {

	private FetchDividendService service;

	@BeforeEach
	void setUp() {
		String bucketName = "fineants2024";
		String dividendPath = "local/dividend/dividends.csv";
		AmazonS3 amazonS3 = Mockito.mock(AmazonS3.class);
		service = new AmazonS3FetchDividendService(bucketName, dividendPath, amazonS3);
	}

	@Test
	void canCreated() {
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void fetchDividend() {
		File file = service.fetchDividend();

		Assertions.assertThat(file).isNotNull();
		FileContentComparator comparator = new FileContentComparator();
		comparator.compare(file, "src/test/resources/gold_dividends.csv");
	}
}
