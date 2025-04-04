package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class NotificationPreferenceNotFoundException extends NotFoundException {
	public NotificationPreferenceNotFoundException(String value) {
		super(value, ErrorCode.NOTIFICATION_PREFERENCE_NOT_FOUND);
	}
}
