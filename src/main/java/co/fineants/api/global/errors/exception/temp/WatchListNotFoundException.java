package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class WatchListNotFoundException extends NotFoundException {
	public WatchListNotFoundException(String value) {
		super(value, CustomErrorCode.WATCH_LIST_NOT_FOUND);
	}
}
