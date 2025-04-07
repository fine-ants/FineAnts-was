package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class NotificationNotFoundException extends NotFoundException {
	public NotificationNotFoundException(String value) {
		super(value, ErrorCode.NOTIFICATION_NOT_FOUND);
	}
}
