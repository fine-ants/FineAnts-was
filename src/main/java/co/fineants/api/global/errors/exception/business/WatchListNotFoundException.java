package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class WatchListNotFoundException extends NotFoundException {
	public WatchListNotFoundException(String value) {
		super(value, ErrorCode.WATCH_LIST_NOT_FOUND);
	}
}
