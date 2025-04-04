package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class WatchListAuthorizationException extends AuthorizationException {
	public WatchListAuthorizationException(String value) {
		super(value, ErrorCode.WATCH_LIST_AUTHORIZATION);
	}
}
