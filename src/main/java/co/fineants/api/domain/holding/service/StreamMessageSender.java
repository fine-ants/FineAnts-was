package co.fineants.api.domain.holding.service;

import java.util.function.Consumer;

import co.fineants.api.domain.holding.domain.message.StreamMessage;

public interface StreamMessageSender extends Consumer<StreamMessage> {

	@Override
	void accept(StreamMessage message);
}
