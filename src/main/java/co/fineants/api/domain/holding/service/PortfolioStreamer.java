package co.fineants.api.domain.holding.service;

import java.time.LocalDateTime;

import co.fineants.api.domain.holding.domain.message.PortfolioStreamMessage;
import reactor.core.publisher.Flux;

public interface PortfolioStreamer {

	Flux<PortfolioStreamMessage> streamMessages(Long portfolioId);

	boolean supports(LocalDateTime time);
}
