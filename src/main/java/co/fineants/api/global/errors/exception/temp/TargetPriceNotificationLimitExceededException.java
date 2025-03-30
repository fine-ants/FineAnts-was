package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class TargetPriceNotificationLimitExceededException extends LimitExceededException {
	public TargetPriceNotificationLimitExceededException(int size) {
		super(String.valueOf(size), CustomErrorCode.TARGET_PRICE_NOTIFICATION_SIZE_LIMIT_EXCEEDED);
	}
}
