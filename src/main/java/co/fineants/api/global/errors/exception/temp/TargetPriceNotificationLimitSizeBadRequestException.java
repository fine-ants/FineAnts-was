package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class TargetPriceNotificationLimitSizeBadRequestException extends BadRequestException {
	public TargetPriceNotificationLimitSizeBadRequestException(int size) {
		super(String.valueOf(size), CustomErrorCode.TARGET_PRICE_NOTIFICATION_LIMIT_SIZE_BAD_REQUEST);
	}
}
