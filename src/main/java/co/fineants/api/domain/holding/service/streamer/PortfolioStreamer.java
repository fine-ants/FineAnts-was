package co.fineants.api.domain.holding.service.streamer;

import java.time.LocalDateTime;

import co.fineants.api.domain.holding.domain.message.StreamMessage;
import reactor.core.publisher.Flux;

public interface PortfolioStreamer {

	Flux<StreamMessage> streamMessages(Long portfolioId);

	boolean supports(LocalDateTime time);
}
