package co.fineants.api.domain.holding.domain.factory;

import java.time.LocalDateTime;
import java.util.List;

import co.fineants.api.domain.holding.service.PortfolioStreamer;
import co.fineants.api.global.common.time.LocalDateTimeService;

public class MarketStatusBasedPortfolioStreamerFactory implements PortfolioStreamerFactory {

	private final List<PortfolioStreamer> streamers;
	private final LocalDateTimeService localDateTimeService;

	public MarketStatusBasedPortfolioStreamerFactory(List<PortfolioStreamer> streamers,
		LocalDateTimeService localDateTimeService) {
		this.streamers = streamers;
		this.localDateTimeService = localDateTimeService;
	}

	@Override
	public PortfolioStreamer getStreamer() throws IllegalStateException {
		LocalDateTime now = localDateTimeService.getLocalDateTimeWithNow();
		return streamers.stream()
			.filter(s -> s.supports(now))
			.findFirst()
			.orElseThrow(
				() -> new IllegalStateException("No suitable PortfolioStreamer found for the current time: " + now));
	}
}
