package co.fineants.api.domain.member.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class NotificationPreference {

	@Column(name = "browser_notify", nullable = false)
	private boolean browserNotify;

	@Column(name = "target_gain_notify", nullable = false)
	private boolean targetGainNotify;

	@Column(name = "max_loss_notify", nullable = false)
	private boolean maxLossNotify;

	@Column(name = "target_price_notify", nullable = false)
	private boolean targetPriceNotify;

	public NotificationPreference(boolean browserNotify, boolean targetGainNotify, boolean maxLossNotify,
		boolean targetPriceNotify) {
		this.browserNotify = browserNotify;
		this.targetGainNotify = targetGainNotify;
		this.maxLossNotify = maxLossNotify;
		this.targetPriceNotify = targetPriceNotify;
	}

	public static NotificationPreference allActive() {
		return new NotificationPreference(true, true, true, true);
	}

	public static NotificationPreference defaultSetting() {
		return new NotificationPreference(false, false, false, false);
	}

	public static NotificationPreference create(boolean browserNotify, boolean targetGainNotify, boolean maxLossNotify,
		boolean targetPriceNotify) {
		return new NotificationPreference(browserNotify, targetGainNotify, maxLossNotify, targetPriceNotify);
	}

	public void changePreference(NotificationPreference notificationPreference) {
		this.browserNotify = notificationPreference.browserNotify;
		this.targetGainNotify = notificationPreference.targetGainNotify;
		this.maxLossNotify = notificationPreference.maxLossNotify;
		this.targetPriceNotify = notificationPreference.targetPriceNotify;
	}

	public boolean isAllInActive() {
		return !this.browserNotify
			&& !this.targetGainNotify
			&& !this.maxLossNotify
			&& !this.targetPriceNotify;
	}

	public boolean isPossibleTargetGainNotification() {
		return this.browserNotify && this.targetGainNotify;
	}

	public boolean isPossibleMaxLossNotification() {
		return this.browserNotify && this.maxLossNotify;
	}

	public boolean isPossibleStockTargetPriceNotification() {
		return this.browserNotify && this.targetPriceNotify;
	}
}
