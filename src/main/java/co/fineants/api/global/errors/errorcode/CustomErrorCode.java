package co.fineants.api.global.errors.errorcode;

public enum CustomErrorCode {
	// Business
	DUPLICATE("B-001", "Duplicate Value"),
	UNAUTHENTICATED("B-002", "Unauthenticated Value"),
	AUTHORIZATION("B-003", "Authorization Error"),

	// Member
	EMAIL_DUPLICATE("M-001", "Duplicate Email"),
	NICKNAME_DUPLICATE("M-002", "Duplicate Nickname"),
	PASSWORD_UNAUTHENTICATED("M-003", "Unauthenticated Password"),

	// Portfolio
	PORTFOLIO_NAME_DUPLICATE("P-001", "Duplicate Name"),
	PORTFOLIO_NOT_FOUND("P-002", "Portfolio Not Found"),

	// Stock
	STOCK_NOT_FOUND("S-001", "Stock Not Found"),

	// Holding
	HOLDING_NOT_FOUND("H-001", "Holding Not Found"),

	// WatchStock
	WATCH_STOCK_DUPLICATE("W-001", "Duplicate WatchStock"),

	// ObjectMapper,
	OBJECT_MAPPER_ERROR("O-001", "ObjectMapper Error");

	private final String code;
	private final String message;

	CustomErrorCode(String code, String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public String toString() {
		return "(code=%s, message=%s)".formatted(code, message);
	}
}
