package co.fineants.api.domain.holding.domain.factory;

import java.time.LocalDateTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import co.fineants.api.domain.holding.service.MarketStatusChecker;
import co.fineants.api.domain.holding.service.PortfolioHoldingService;
import co.fineants.api.domain.holding.service.streamer.FluxIntervalPortfolioStreamer;
import co.fineants.api.domain.holding.service.streamer.PortfolioStreamer;
import co.fineants.api.global.common.time.LocalDateTimeService;

class MarketStatusBasedPortfolioStreamerFactoryTest {

	private PortfolioStreamerFactory factory;
	private LocalDateTimeService localDateTimeService;

	@BeforeEach
	void setUp() {
		PortfolioHoldingService portfolioHoldingService = Mockito.mock(PortfolioHoldingService.class);
		MarketStatusChecker stockMarketChecker = new MarketStatusChecker();
		int intervalSecond = 5;
		int maxCount = 6;
		List<PortfolioStreamer> streamers = List.of(
			new FluxIntervalPortfolioStreamer(portfolioHoldingService, stockMarketChecker, intervalSecond, maxCount)
		);
		localDateTimeService = Mockito.mock(LocalDateTimeService.class);
		factory = new MarketStatusBasedPortfolioStreamerFactory(streamers, localDateTimeService);
	}

	@DisplayName("장시간 내에서는 FluxIntervalPortfolioStreamer를 반환한다.")
	@Test
	void whenMarketIsOpen_thenReturnsFluxIntervalPortfolioStreamer() {
		// given
		LocalDateTime time = LocalDateTime.of(2025, 6, 4, 9, 30);
		BDDMockito.given(localDateTimeService.getLocalDateTimeWithNow())
			.willReturn(time);
		// when
		PortfolioStreamer streamer = factory.getStreamer();
		// then
		Assertions.assertThat(streamer)
			.isInstanceOf(FluxIntervalPortfolioStreamer.class);
	}

}
