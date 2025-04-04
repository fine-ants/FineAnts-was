package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class WatchListAuthorizationException extends AuthorizationException {
	public WatchListAuthorizationException(String value) {
		super(value, CustomErrorCode.WATCH_LIST_AUTHORIZATION);
	}
}
