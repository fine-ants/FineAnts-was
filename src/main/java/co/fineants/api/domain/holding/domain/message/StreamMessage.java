package co.fineants.api.domain.holding.domain.message;

public interface StreamMessage {
	Object getData();

	String getEventName();
}
