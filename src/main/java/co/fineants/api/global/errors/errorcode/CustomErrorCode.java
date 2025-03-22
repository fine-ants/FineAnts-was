package co.fineants.api.global.errors.errorcode;

public enum CustomErrorCode {
	// Business
	DUPLICATE("B-001", "Duplicate Value");

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
