package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class NotificationNotFoundException extends NotFoundException {
	public NotificationNotFoundException(String value) {
		super(value, CustomErrorCode.NOTIFICATION_NOT_FOUND);
	}
}
