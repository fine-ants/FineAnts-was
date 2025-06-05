package co.fineants.api.domain.holding.service;

import java.util.function.Consumer;

import co.fineants.api.domain.holding.domain.message.PortfolioStreamMessage;

public interface PortfolioStreamMessageConsumer extends Consumer<PortfolioStreamMessage> {

	@Override
	void accept(PortfolioStreamMessage message);
}
