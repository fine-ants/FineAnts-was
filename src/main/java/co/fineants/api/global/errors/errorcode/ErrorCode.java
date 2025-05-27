package co.fineants.api.global.errors.errorcode;

import lombok.Getter;

@Getter
public enum ErrorCode {
	// Business
	DUPLICATE("B-001", "Duplicate Value"),
	UNAUTHORIZED("B-002", "Unauthorized Value"),
	FORBIDDEN("B-003", "Forbidden Value"),
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
	MEMBER_PROFILE_NOT_CHANGE_BAD_REQUEST("M-014", "Member Profile Not Change"),
	MEMBER_AUTHENTICATION("M-015", "Member Authentication Error"),
	LOGIN_FAIL("M-016", "Login Fail"),
	SIGNUP_FAIL("M-017", "Signup Fail"),
	// Portfolio
	PORTFOLIO_NAME_DUPLICATE("P-001", "Duplicate Name"),
	PORTFOLIO_NOT_FOUND("P-002", "Portfolio Not Found"),
	SECURITIES_FIRM_BAD_REQUEST("P-003", "Bad Request Securities Firm"),
	PORTFOLIO_BAD_REQUEST("P-004", "Bad Request Portfolio"),
	CASH_NOT_SUFFICIENT_FOR_PURCHASE("P-005", "Cash Not Sufficient For Purchase"),
	TARGET_GAIN_LESS_THAN_BUDGET("P-006", "Target Gain Less Than Budget"),
	MAXIMUM_LOSS_GREATER_THAN_BUDGET("P-007", "Maximum Loss Greater Than Budget"),
	TARGET_GAIN_NOTIFICATION_ACTIVE_NOT_CHANGE("P-008", "Target Gain Notification Active Not Change"),
	MAXIMUM_LOSS_NOTIFICATION_ACTIVE_NOT_CHANGE("P-009", "Maximum Loss Notification Active Not Change"),
	PORTFOLIO_NAME_INVALID("P-010", "Invalid Portfolio Name"),
	SECURITIES_FIRM_NOT_CONTAIN("P-011", "Unlisted Securities Firm"),
	// Stock
	STOCK_NOT_FOUND("S-001", "Stock Not Found"),
	// Holding
	HOLDING_NOT_FOUND("H-001", "Holding Not Found"),
	// PurchaseHistory
	PURCHASE_HISTORY_BAD_REQUEST("PH-001", "PurchaseHistory Bad Request"),
	PURCHASE_HISTORY_NOT_FOUND("PH-002", "PurchaseHistory Not Found"),
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
	WATCH_LIST_AUTHORIZATION("WL-002", "WatchList Authorization Error"),
	// Mail
	MAIL_BAD_REQUEST("MAIL-001", "Mail Bad Request"),
	// External API
	EXTERNAL_API_GET_REQUEST("EX-001", "External API Get Request Error"),
	// Exchange Rate
	EXCHANGE_RATE_DUPLICATE("EX-001", "Duplicate Exchange Rate"),
	BASE_EXCHANGE_RATE_NOT_FOUND("EX-002", "Base Exchange Rate Not Found"),
	EXCHANGE_RATE_NOT_FOUND("EX-003", "Exchange Rate Not Found"),
	BASE_EXCHANGE_RATE_DELETE_BAD_REQUEST("EX-004", "Base Exchange Rate Delete Bad Request"),
	EXCHANGE_RATE_RAPID_API_UNKNOWN("EX-005", "Exchange Rate Rapid API Unknown Error"),
	EXCHANGE_RATE_RAPID_API_INVALID_API_KEY("EX-006", "Exchange Rate Rapid API Invalid API Key"),
	EXCHANGE_RATE_RAPID_API_REQUEST_EXCEEDED("EX-007", "Exchange Rate Rapid API Request Exceeded"),
	EXCHANGE_RATE_RAPID_API_INVALID_SIGN("EX-008", "Exchange Rate Rapid API Invalid Sign"),
	EXCHANGE_RATE_RAPID_API_INVALID_CURRENCY_CODE("EX-009", "Exchange Rate Rapid API Invalid Currency Code"),
	EXCHANGE_RATE_RAPID_API_NETWORK_ANOMALY("EX-010", "Exchange Rate Rapid API Network Anomaly"),
	EXCHANGE_RATE_RAPID_API_QUERY_FAILED("EX-011", "Exchange Rate Rapid API Query Failed"),
	// Fcm
	FCM_DUPLICATE("FCM-001", "Duplicate FCM Token"),
	FCM_BAD_REQUEST("FCM-002", "Bad Request FCM Token"),
	// Role
	ROLE_NOT_FOUND("R-001", "Role Not Found"),
	// Money
	MONEY_NEGATIVE("M-001", "Money cannot be negative"),
	// KIS
	KIS_DEFAULT_ERROR("KIS-001", "KIS Default Error"),
	KIS_CREDENTIALS_TYPE_ERROR("KIS-002", "KIS Credentials Type Error"),
	KIS_REQUEST_LIMIT_EXCEEDED("KIS-003", "KIS Request Limit Exceeded"),
	KIS_TOKEN_ISSUANCE_RETRY_LATER("KIS-004", "KIS Token Issuance Retry Later"),
	KIS_EXPIRED_ACCESS_TOKEN("KIS-005", "KIS Expired Access Token");

	private final String code;
	private final String message;

	ErrorCode(String code, String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public String toString() {
		return "(code=%s, message=%s)".formatted(code, message);
	}
}
