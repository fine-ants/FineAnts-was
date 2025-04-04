package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class TargetPriceNotificationNotFoundException extends NotFoundException {
	public TargetPriceNotificationNotFoundException(String value) {
		super(value, ErrorCode.TARGET_PRICE_NOTIFICATION_NOT_FOUND);
	}
}
