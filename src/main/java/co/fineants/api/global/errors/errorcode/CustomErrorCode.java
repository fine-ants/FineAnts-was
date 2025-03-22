package co.fineants.api.global.errors.errorcode;

public enum CustomErrorCode {
	// Business
	DUPLICATE("B-001", "Duplicate Value"),

	// Member
	EMAIL_DUPLICATE("M-001", "Duplicate Email"),
	NICKNAME_DUPLICATE("M-002", "Duplicate Nickname");

	private final String code;
	private final String message;

	CustomErrorCode(String code, String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public String toString() {
		return "ErrorCode(code=%s, message=%s)".formatted(code, message);
	}
}
