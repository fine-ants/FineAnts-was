package co.fineants.api.domain.notification.domain.entity.policy;

import java.util.List;
import java.util.function.Predicate;

import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.member.domain.entity.NotificationPreference;

public class TargetPriceNotificationPolicy extends AbstractNotificationPolicy {

	public TargetPriceNotificationPolicy(
		List<Predicate<Notifiable>> conditions,
		Predicate<NotificationPreference> preference
	) {
		super(conditions, preference);
	}
}
