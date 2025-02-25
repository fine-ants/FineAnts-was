package co.fineants.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import co.fineants.api.domain.dividend.domain.calculator.ExDividendDateCalculator;
import co.fineants.api.domain.dividend.repository.StockDividendRepository;
import co.fineants.api.domain.dividend.service.StockDividendService;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.infra.s3.service.AmazonS3DividendService;
import lombok.RequiredArgsConstructor;

@TestConfiguration
@RequiredArgsConstructor
public class StockDividendServiceConfig {
	private final AmazonS3DividendService s3DividendService;
	private final StockRepository stockRepository;
	private final StockDividendRepository stockDividendRepository;
	private final ExDividendDateCalculator exDividendDateCalculator;

	@Bean
	public StockDividendService stockDividendService() {
		LocalDateTimeService mockedLocalDateTimeService = mockedLocalDateTimeService();
		KisService mockedKisService = mockedKisService();
		return new StockDividendService(
			s3DividendService,
			stockRepository,
			stockDividendRepository,
			mockedKisService,
			mockedLocalDateTimeService,
			exDividendDateCalculator
		);
	}

	@Bean
	@Primary
	public LocalDateTimeService mockedLocalDateTimeService() {
		return Mockito.mock(LocalDateTimeService.class);
	}

	@Bean
	@Primary
	public KisService mockedKisService() {
		return Mockito.mock(KisService.class);
	}
}
