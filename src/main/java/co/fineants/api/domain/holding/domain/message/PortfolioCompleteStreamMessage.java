package co.fineants.api.domain.holding.domain.message;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class PortfolioCompleteStreamMessage implements StreamMessage {
	private final String message;

	public PortfolioCompleteStreamMessage() {
		this.message = "sse complete";
	}

	@Override
	public Object getData() {
		return message;
	}

	@Override
	public String getEventName() {
		return "complete";
	}

	@Override
	public String toString() {
		return String.format("포트폴리오 완료 메시지 - %s", message);
	}
}
