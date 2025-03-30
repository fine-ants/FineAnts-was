package co.fineants.api.global.errors.errorcode;

import lombok.Getter;

@Getter
public enum CustomErrorCode {
	// Business
	DUPLICATE("B-001", "Duplicate Value"),
	UNAUTHENTICATED("B-002", "Unauthenticated Value"),
	AUTHORIZATION("B-003", "Authorization Error"),
	NOT_FOUND("B-004", "Not Found"),

	// Member
	EMAIL_DUPLICATE("M-001", "Duplicate Email"),
	NICKNAME_DUPLICATE("M-002", "Duplicate Nickname"),
	PASSWORD_UNAUTHENTICATED("M-003", "Unauthenticated Password"),
	MEMBER_NOT_FOUND("M-004", "Member Not Found"),

	// Portfolio
	PORTFOLIO_NAME_DUPLICATE("P-001", "Duplicate Name"),
	PORTFOLIO_NOT_FOUND("P-002", "Portfolio Not Found"),

	// Stock
	STOCK_NOT_FOUND("S-001", "Stock Not Found"),

	// Holding
	HOLDING_NOT_FOUND("H-001", "Holding Not Found"),

	// WatchStock
	WATCH_STOCK_DUPLICATE("W-001", "Duplicate WatchStock"),

	// NotificationPreference
	NOTIFICATION_PREFERENCE_NOT_FOUND("NP-001", "NotificationPreference Not Found"),

	// ObjectMapper,
	OBJECT_MAPPER_ERROR("O-001", "ObjectMapper Error"),

	// Notification
	NOTIFICATION_NOT_FOUND("N-001", "Notification Not Found"),
	// StockTargetPrice
	STOCK_TARGET_PRICE_NOT_FOUND("STP-001", "StockTargetPrice Not Found"),

	// TargetPriceNotification
	TARGET_PRICE_NOTIFICATION_NOT_FOUND("TPN-001", "TargetPriceNotification Not Found"),

	// WatchList
	WATCH_LIST_NOT_FOUND("WL-001", "WatchList Not Found");

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
