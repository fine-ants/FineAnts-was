package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class TargetPriceNotificationLimitExceededException extends LimitExceededException {
	public TargetPriceNotificationLimitExceededException(int size) {
		super(String.valueOf(size), ErrorCode.TARGET_PRICE_NOTIFICATION_SIZE_LIMIT_EXCEEDED);
	}
}
