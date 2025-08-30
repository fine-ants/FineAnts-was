package co.fineants.api.domain.holding.domain.factory;

import co.fineants.api.domain.holding.service.streamer.PortfolioStreamer;

public class AlwaysOpenPortfolioStreamerFactory implements PortfolioStreamerFactory {

	private final PortfolioStreamer portfolioStreamer;

	public AlwaysOpenPortfolioStreamerFactory(PortfolioStreamer portfolioStreamer) {
		this.portfolioStreamer = portfolioStreamer;
	}

	@Override
	public PortfolioStreamer getStreamer() throws IllegalStateException {
		return portfolioStreamer;
	}
}
