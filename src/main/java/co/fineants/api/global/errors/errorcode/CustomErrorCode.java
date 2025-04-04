package co.fineants.api.global.errors.errorcode;

import lombok.Getter;

@Getter
public enum CustomErrorCode {
	// Business
	DUPLICATE("B-001", "Duplicate Value"),
	UNAUTHENTICATED("B-002", "Unauthenticated Value"),
	AUTHORIZATION("B-003", "Authorization Error"),
	NOT_FOUND("B-004", "Not Found"),
	BAD_REQUEST("B-005", "Bad Request"),
	EXTERNAL_API_REQUEST("B-006", "External API Request Error"),
	// Member
	EMAIL_DUPLICATE("M-001", "Duplicate Email"),
	NICKNAME_DUPLICATE("M-002", "Duplicate Nickname"),
	PASSWORD_UNAUTHENTICATED("M-003", "Unauthenticated Password"),
	MEMBER_NOT_FOUND("M-004", "Member Not Found"),
	NICKNAME_BAD_REQUEST("M-005", "Bad Request Nickname"),
	EMAIL_BAD_REQUEST("M-006", "Bad Request Email"),
	PASSWORD_BAD_REQUEST("M-007", "Not Match Password"),
	PASSWORD_CONFIRM_BAD_REQUEST("M-008", "Not Match Password Confirm"),
	VERIFY_CODE_BAD_REQUEST("M-009", "Not Match Verify Code"),
	IMAGE_SIZE_EXCEEDED_BAD_REQUEST("M-010", "Image Size Exceeded"),
	IMAGE_NAME_EMPTY_BAD_REQUEST("M-011", "Image Name Empty"),
	IMAGE_WRITE_BAD_REQUEST("M-012", "Image Write Error"),
	IMAGE_EMPTY_BAD_REQUEST("M-013", "Image Empty"),
	// Portfolio
	PORTFOLIO_NAME_DUPLICATE("P-001", "Duplicate Name"),
	PORTFOLIO_NOT_FOUND("P-002", "Portfolio Not Found"),
	SECURITIES_FIRM_BAD_REQUEST("P-003", "Bad Request Securities Firm"),
	PORTFOLIO_BAD_REQUEST("P-004", "Bad Request Portfolio"),
	// Stock
	STOCK_NOT_FOUND("S-001", "Stock Not Found"),
	// Holding
	HOLDING_NOT_FOUND("H-001", "Holding Not Found"),
	// PurchaseHistory
	PURCHASE_HISTORY_BAD_REQUEST("PH-001", "PurchaseHistory Bad Request"),
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
	TARGET_PRICE_NOTIFICATION_SIZE_LIMIT_EXCEEDED("TPN-002", "TargetPriceNotification Limit Size's Exceeded"),
	TARGET_PRICE_NOTIFICATION_DUPLICATE("TPN-003", "TargetPriceNotification Duplicate"),
	// WatchList
	WATCH_LIST_NOT_FOUND("WL-001", "WatchList Not Found"),
	// Mail
	MAIL_BAD_REQUEST("MAIL-001", "Mail Bad Request"),
	// External API
	EXTERNAL_API_GET_REQUEST("EX-001", "External API Get Request Error"),
	// Exchange Rate
	EXCHANGE_RATE_DUPLICATE("EX-001", "Duplicate Exchange Rate"),
	BASE_EXCHANGE_RATE_NOT_FOUND("EX-002", "Base Exchange Rate Not Found"),
	EXCHANGE_RATE_NOT_FOUND("EX-003", "Exchange Rate Not Found"),
	BASE_EXCHANGE_RATE_DELETE_BAD_REQUEST("EX-004", "Base Exchange Rate Delete Bad Request"),
	// Fcm
	FCM_DUPLICATE("FCM-001", "Duplicate FCM Token"),
	FCM_BAD_REQUEST("FCM-002", "Bad Request FCM Token");

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
