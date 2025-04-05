package co.fineants.api.domain.kis.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.api.global.errors.exception.temp.CredentialsTypeKisException;
import co.fineants.api.global.errors.exception.temp.DefaultKisApiRequestException;
import co.fineants.api.global.errors.exception.temp.ExpiredAccessTokenKisException;
import co.fineants.api.global.errors.exception.temp.KisApiRequestException;
import co.fineants.api.global.errors.exception.temp.RequestLimitExceededKisException;
import co.fineants.api.global.errors.exception.temp.TokenIssuanceRetryLaterKisException;
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

	public KisApiRequestException toException() {
		return switch (messageCode) {
			case "EGW00201" -> new RequestLimitExceededKisException(returnCode, messageCode, message);
			case "EGW00133" -> new TokenIssuanceRetryLaterKisException(returnCode, messageCode, message);
			case "EGW00123" -> new ExpiredAccessTokenKisException(returnCode, messageCode, message);
			case "EGW00205" -> new CredentialsTypeKisException(returnCode, messageCode, message);
			default -> new DefaultKisApiRequestException(returnCode, messageCode, message);
		};
	}

	public boolean isRequestLimitExceeded() {
		return "EGW00201".equals(messageCode);
	}
}
