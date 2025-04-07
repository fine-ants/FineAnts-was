package co.fineants.api.global.errors.exception.domain;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.global.errors.errorcode.ErrorCode;

public class TargetGainNotificationActiveNotChangeException extends DomainException {
	private final Money targetGain;

	public TargetGainNotificationActiveNotChangeException(Money targetGain) {
		super(String.format("Target gain notification active cannot be changed: %s", targetGain),
			ErrorCode.TARGET_GAIN_NOTIFICATION_ACTIVE_NOT_CHANGE);
		this.targetGain = targetGain;
	}

	@Override
	public String toString() {
		return String.format("TargetGainNotificationActiveNotChangeException(targetGain=%s, %s)", targetGain,
			super.toString());
	}
}
