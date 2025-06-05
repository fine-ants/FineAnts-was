package co.fineants.api.domain.holding.service.streamer;

import java.time.LocalDateTime;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.factory.StreamMessageConsumerFactory;
import co.fineants.api.domain.holding.domain.message.PortfolioCompleteStreamMessage;
import co.fineants.api.domain.holding.domain.message.StreamMessage;
import co.fineants.api.domain.holding.service.market_status_checker.MarketStatusChecker;
import co.fineants.api.domain.holding.service.sender.StreamSseMessageSender;
import reactor.core.publisher.Flux;

public class ClosedMarketPortfolioStreamer implements PortfolioStreamer {

	private final MarketStatusChecker marketStatusChecker;

	public ClosedMarketPortfolioStreamer(MarketStatusChecker marketStatusChecker) {
		this.marketStatusChecker = marketStatusChecker;
	}

	@Override
	public Flux<StreamMessage> streamMessages(Long portfolioId) {
		return Flux.just(new PortfolioCompleteStreamMessage());
	}

	@Override
	public boolean supports(LocalDateTime time) {
		return !marketStatusChecker.isOpen(time);
	}

	@Override
	public StreamSseMessageSender createStreamSseMessageSender(SseEmitter emitter,
		StreamMessageConsumerFactory factory) {
		return factory.createStreamCompleteMessageSender(emitter);
	}
}
