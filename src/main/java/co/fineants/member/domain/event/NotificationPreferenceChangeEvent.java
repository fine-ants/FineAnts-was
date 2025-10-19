package co.fineants.member.domain.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class NotificationPreferenceChangeEvent extends ApplicationEvent {
	private final Long memberId;
	private final Long fcmTokenId;

	public NotificationPreferenceChangeEvent(Long memberId, Long fcmTokenId) {
		super(System.currentTimeMillis());
		this.memberId = memberId;
		this.fcmTokenId = fcmTokenId;
	}
}
