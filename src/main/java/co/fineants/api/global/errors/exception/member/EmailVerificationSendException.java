package co.fineants.api.global.errors.exception.member;

public class EmailVerificationSendException extends RuntimeException {
	public EmailVerificationSendException(String message, Throwable cause) {
		super(message, cause);
	}
}
