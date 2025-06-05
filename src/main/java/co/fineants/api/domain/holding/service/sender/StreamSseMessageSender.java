package co.fineants.api.domain.holding.service.sender;

import java.util.function.Consumer;

import co.fineants.api.domain.holding.domain.message.StreamMessage;

public interface StreamSseMessageSender extends Consumer<StreamMessage> {

	@Override
	void accept(StreamMessage message);
}
