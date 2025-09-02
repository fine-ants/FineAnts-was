package co.fineants.api.infra.s3.service;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import co.fineants.api.domain.dividend.domain.calculator.ExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.calculator.FileExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.dividend.domain.parser.StockDividendParser;
import co.fineants.api.domain.dividend.domain.reader.HolidayFileReader;
import co.fineants.api.domain.kis.repository.FileHolidayRepository;
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
		StockDividendParser stockDividendParser = createStockDividendParser();
		service = new AmazonS3FetchDividendService(fileFetcher, dividendPath, stockDividendParser);
	}

	private StockDividendParser createStockDividendParser() {
		FileHolidayRepository fileHolidayRepository = new FileHolidayRepository(new HolidayFileReader());
		ExDividendDateCalculator exDividendDateCalculator = new FileExDividendDateCalculator(fileHolidayRepository);
		return new StockDividendParser(exDividendDateCalculator);
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

	@Test
	void fetchDividendEntity() {
		List<StockDividend> list = service.fetchDividendEntityIn(List.of());

		Assertions.assertThat(list).isEmpty();
	}
}
