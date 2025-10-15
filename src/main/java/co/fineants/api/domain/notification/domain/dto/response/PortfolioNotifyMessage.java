package co.fineants.api.domain.notification.domain.dto.response;

import java.util.List;

import co.fineants.member.domain.Member;
import co.fineants.api.domain.notification.domain.entity.Notification;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder(toBuilder = true)
public class PortfolioNotifyMessage extends NotifyMessage {
	private final String name;
	private final Long portfolioId;

	@Override
	public NotifyMessage withMessageId(List<String> messageIds) {
		return this.toBuilder()
			.messageIds(messageIds)
			.build();
	}

	@Override
	public Notification toEntity(Member member) {
		return Notification.portfolioNotification(
			getTitle(),
			getType(),
			getReferenceId(),
			getLink(),
			member,
			getMessageIds(),
			name,
			portfolioId
		);
	}
}
