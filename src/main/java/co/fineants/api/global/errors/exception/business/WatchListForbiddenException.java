package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class WatchListForbiddenException extends ForbiddenException {
	public WatchListForbiddenException(String value) {
		super(value, ErrorCode.WATCH_LIST_AUTHORIZATION);
	}
}
