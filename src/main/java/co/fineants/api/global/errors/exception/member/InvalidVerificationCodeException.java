package co.fineants.api.global.errors.exception.member;

public class InvalidVerificationCodeException extends RuntimeException {
	public InvalidVerificationCodeException(String message) {
		super(message);
	}
}
