package co.fineants.api.domain.holding.domain.factory;

import co.fineants.api.domain.holding.service.streamer.PortfolioStreamer;

public interface PortfolioStreamerFactory {

	PortfolioStreamer getStreamer() throws IllegalStateException;
}
