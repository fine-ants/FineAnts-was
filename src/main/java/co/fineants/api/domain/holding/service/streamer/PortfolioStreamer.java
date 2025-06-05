package co.fineants.api.domain.holding.service.streamer;

import java.time.LocalDateTime;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.factory.StreamMessageConsumerFactory;
import co.fineants.api.domain.holding.domain.message.StreamMessage;
import co.fineants.api.domain.holding.service.sender.StreamSseMessageSender;
import reactor.core.publisher.Flux;

public interface PortfolioStreamer {

	Flux<StreamMessage> streamMessages(Long portfolioId);

	boolean supports(LocalDateTime time);

	StreamSseMessageSender createStreamSseMessageSender(SseEmitter emitter, StreamMessageConsumerFactory factory);
}
