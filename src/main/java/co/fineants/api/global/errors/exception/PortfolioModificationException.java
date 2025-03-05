package co.fineants.api.global.errors.exception;

import org.springframework.http.HttpStatus;

public class PortfolioModificationException extends FineAntsException {
	public PortfolioModificationException(HttpStatus httpStatus, String message, Throwable cause) {
		super(httpStatus, message, cause);
	}
}
