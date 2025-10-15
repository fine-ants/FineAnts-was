package co.fineants.api.domain.notification.domain.entity.policy;

import java.util.List;
import java.util.function.Predicate;

import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.member.domain.NotificationPreference;

public class MaxLossNotificationPolicy extends AbstractNotificationPolicy {

	public MaxLossNotificationPolicy(
		List<Predicate<Notifiable>> conditions,
		Predicate<NotificationPreference> preference
	) {
		super(conditions, preference);
	}
}
