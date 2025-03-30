package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class NotificationPreferenceNotFoundException extends NotFoundException {
	public NotificationPreferenceNotFoundException(String value) {
		super(value, CustomErrorCode.NOTIFICATION_PREFERENCE_NOT_FOUND);
	}
}
