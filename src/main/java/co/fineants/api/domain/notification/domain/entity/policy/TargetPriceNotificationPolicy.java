package co.fineants.api.domain.notification.domain.entity.policy;

import java.util.List;
import java.util.function.Predicate;

import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TargetPriceNotificationPolicy implements NotificationPolicy {

	private final List<Predicate<Notifiable>> targetPriceConditions;
	private final Predicate<NotificationPreference> preferenceConditions;

	@Override
	public boolean isSatisfied(Notifiable target) {
		boolean isTargetPriceValid = targetPriceConditions.stream()
			.allMatch(condition -> condition.test(target));
		boolean isPreferenceValid = preferenceConditions.test(target.getNotificationPreference());
		return isTargetPriceValid && isPreferenceValid;
	}
}
