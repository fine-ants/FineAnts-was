package co.fineants.api.infra.s3.service;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;

class AmazonS3FetchDividendServiceTest {

	private FetchDividendService service;

	@BeforeEach
	void setUp() {
		RemoteFileFetcher fileFetcher = Mockito.mock(AmazonS3RemoteFileFetcher.class);
		String dividendPath = "local/dividend/dividends.csv";
		service = new AmazonS3FetchDividendService(fileFetcher, dividendPath);
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
