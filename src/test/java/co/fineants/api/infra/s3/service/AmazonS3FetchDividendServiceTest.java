package co.fineants.api.infra.s3.service;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import co.fineants.api.infra.s3.dto.StockDividendDto;

class AmazonS3FetchDividendServiceTest {

	private FetchDividendService service;

	private InputStream getMockInputStream() {
		try {
			return new java.io.FileInputStream("src/test/resources/gold_dividends.csv");
		} catch (java.io.FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@BeforeEach
	void setUp() {
		RemoteFileFetcher fileFetcher = Mockito.mock(AmazonS3RemoteFileFetcher.class);
		String dividendPath = "local/dividend/dividends.csv";
		BDDMockito.given(fileFetcher.read(dividendPath))
			.willReturn(getMockInputStream());
		service = new AmazonS3FetchDividendService(fileFetcher, dividendPath);
	}

	@Test
	void canCreated() {
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void fetchDividend() {
		List<StockDividendDto> list = service.fetchDividend();

		Assertions.assertThat(list)
			.isNotNull()
			.allMatch(Objects::nonNull);
		StockDividendDto dto = new StockDividendDto(1L, 361, "20230331", "20230517", "KR7005930003");
		Assertions.assertThat(list)
			.hasSize(1)
			.containsExactly(dto);
	}
}
