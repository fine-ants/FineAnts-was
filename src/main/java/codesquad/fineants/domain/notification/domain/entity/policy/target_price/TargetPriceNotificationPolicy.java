package codesquad.fineants.domain.notification.domain.entity.policy.target_price;

import java.util.List;
import java.util.Optional;

import codesquad.fineants.domain.common.notification.Notifiable;
import codesquad.fineants.domain.notification.domain.dto.response.NotifyMessage;
import codesquad.fineants.domain.notification.domain.entity.policy.NotificationCondition;
import codesquad.fineants.domain.notification.domain.entity.policy.NotificationPolicy;
import codesquad.fineants.domain.notificationpreference.domain.entity.NotificationPreference;
import codesquad.fineants.domain.stock_target_price.domain.entity.TargetPriceNotification;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TargetPriceNotificationPolicy implements NotificationPolicy<Notifiable> {

	private final List<NotificationCondition<TargetPriceNotification>> targetPriceConditions;
	private final List<NotificationCondition<NotificationPreference>> preferenceConditions;

	@Override
	public Optional<NotifyMessage> apply(Notifiable target, String token) {
		boolean result = targetPriceConditions.stream()
			.allMatch(condition -> condition.isSatisfiedBy((TargetPriceNotification)target))
			&& preferenceConditions.stream()
			.allMatch(condition -> condition.isSatisfiedBy(target.getNotificationPreference()));
		if (result) {
			return Optional.of(target.getTargetPriceMessage(token));
		}
		return Optional.empty();
	}
}
