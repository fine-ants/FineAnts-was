package co.fineants.api.domain.exchangerate.domain.dto.response;

import java.util.Map;

import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.global.errors.exception.business.ExchangeRateRapidApiRequestException;
import co.fineants.api.global.errors.exception.business.InvalidApiKeyExchangeRateRapidApiRequestException;
import co.fineants.api.global.errors.exception.business.InvalidCurrencyCodeExchangeRateRapidApiRequestException;
import co.fineants.api.global.errors.exception.business.InvalidSignExchangeRateRapidApiRequestException;
import co.fineants.api.global.errors.exception.business.NetworkAnomalyExchangeRateRapidApiRequestException;
import co.fineants.api.global.errors.exception.business.QueryFailedExchangeRateRapidApiRequestException;
import co.fineants.api.global.errors.exception.business.RequestExceededExchangeRateRapidApiRequestException;
import co.fineants.api.global.errors.exception.business.UnknownExchangeRateRapidApiRequestException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeRateFetchResponse {
	private String code;
	private String msg;
	private String base;
	private Map<String, Double> rates;

	private ExchangeRateFetchResponse(String code, String msg) {
		this.code = code;
		this.msg = msg;
		this.base = null;
		this.rates = null;
	}

	public static ExchangeRateFetchResponse krw(Map<String, Double> rates) {
		return new ExchangeRateFetchResponse("0", "success", Currency.KRW.name(), rates);
	}

	public static ExchangeRateFetchResponse invalidApiKey() {
		return new ExchangeRateFetchResponse("10001", "Invalid apikey");
	}

	public static ExchangeRateFetchResponse requestExceeded() {
		return new ExchangeRateFetchResponse("10002", "Request exceeded");
	}

	public boolean containsBy(String code) {
		return rates.containsKey(code);
	}

	public Double getBy(String code) {
		return rates.getOrDefault(code, 0.0);
	}

	public boolean isSuccess() {
		return code.equals("0");
	}

	public ExchangeRateRapidApiRequestException toException() {
		return switch (code) {
			case "10001" -> new InvalidApiKeyExchangeRateRapidApiRequestException(code, msg);
			case "10002" -> new RequestExceededExchangeRateRapidApiRequestException(code, msg);
			case "10003" -> new InvalidSignExchangeRateRapidApiRequestException(code, msg);
			case "200501" -> new InvalidCurrencyCodeExchangeRateRapidApiRequestException(code, msg);
			case "200503" -> new NetworkAnomalyExchangeRateRapidApiRequestException(code, msg);
			case "200504" -> new QueryFailedExchangeRateRapidApiRequestException(code, msg);
			default -> new UnknownExchangeRateRapidApiRequestException();
		};
	}
}
