package co.fineants.api.domain.exchangerate.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.exception.business.ExternalApiGetRequestException;

@Profile("local")
public class LocalExchangeRateClient implements ExchangeRateClient {

	private static final String BASE = "USD";
	private final Map<String, Double> rates;

	public LocalExchangeRateClient() {
		this.rates = new HashMap<>();
		this.rates.put(BASE, 1.0);
		this.rates.put("KRW", 1500.0);
	}

	@Override
	public Double fetchRateBy(String code, String base) throws ExternalApiGetRequestException {
		// base 환율을 기준으로 code에 대한 환율을 반환한다
		if (base.equalsIgnoreCase(BASE) || base.equalsIgnoreCase(code)) {
			return rates.get(code);
		}
		double baseRate = rates.get(base);
		double codeRate = rates.get(code);
		return codeRate / baseRate;
	}

	@Override
	public Map<String, Double> fetchRates(String base) {
		if (base.equals("KRW")) {
			return rates;
		} else {
			throw new ExternalApiGetRequestException("Invalid base", HttpStatus.BAD_REQUEST);
		}
	}
}
