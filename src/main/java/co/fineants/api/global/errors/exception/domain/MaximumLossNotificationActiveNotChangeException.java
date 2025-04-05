package co.fineants.api.global.errors.exception.domain;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.global.errors.errorcode.ErrorCode;

public class MaximumLossNotificationActiveNotChangeException extends DomainException {
	private final Money maximumLoss;

	public MaximumLossNotificationActiveNotChangeException(Money maximumLoss) {
		super(String.format("Maximum loss notification active cannot be changed: %s", maximumLoss),
			ErrorCode.MAXIMUM_LOSS_NOTIFICATION_ACTIVE_NOT_CHANGE);
		this.maximumLoss = maximumLoss;
	}

	@Override
	public String toString() {
		return String.format("MaximumLossNotificationActiveNotChangeException(maximumLoss=%s, %s)", maximumLoss,
			super.toString());
	}
}
