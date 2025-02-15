package co.fineants.api.domain.kis.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.api.global.errors.exception.kis.CredentialsTypeKisException;
import co.fineants.api.global.errors.exception.kis.ExpiredAccessTokenKisException;
import co.fineants.api.global.errors.exception.kis.KisException;
import co.fineants.api.global.errors.exception.kis.RequestLimitExceededKisException;
import co.fineants.api.global.errors.exception.kis.TokenIssuanceRetryLaterKisException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class KisErrorResponse {
	@JsonProperty("rt_cd")
	private String returnCode;
	@JsonProperty("msg_cd")
	private String messageCode;
	@JsonProperty("msg1")
	private String message;

	public KisException toException() {
		return switch (messageCode) {
			case "EGW00201" -> new RequestLimitExceededKisException(returnCode, messageCode, message);
			case "EGW00133" -> new TokenIssuanceRetryLaterKisException(returnCode, messageCode, message);
			case "EGW00123" -> new ExpiredAccessTokenKisException(returnCode, messageCode, message);
			case "EGW00205" -> new CredentialsTypeKisException(returnCode, messageCode, message);
			default -> new KisException(returnCode, messageCode, message);
		};
	}

	public boolean isRequestLimitExceeded() {
		return "EGW00201".equals(messageCode);
	}
}
