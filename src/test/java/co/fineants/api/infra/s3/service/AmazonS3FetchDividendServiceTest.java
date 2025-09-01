package co.fineants.api.infra.s3.service;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.amazonaws.services.s3.AmazonS3;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;

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
		List<StockDividend> list = service.fetchDividend();

		Assertions.assertThat(list).isNotNull();
		Assertions.assertThat(list).hasSize(1);
	}
}
