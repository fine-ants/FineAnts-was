package co.fineants.api.global.errors.exception.member;

public class DuplicateEmailException extends RuntimeException {

	public DuplicateEmailException(String message) {
		super(message);
	}
}
