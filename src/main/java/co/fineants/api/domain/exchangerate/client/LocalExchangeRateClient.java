package co.fineants.api.domain.exchangerate.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.exception.business.ExternalApiGetRequestException;

@Profile("local")
public class LocalExchangeRateClient implements ExchangeRateClient {

	private final Map<String, Double> rates;

	public LocalExchangeRateClient() {
		this.rates = new HashMap<>();
		this.rates.put("KRW", 1.0);
		this.rates.put("USD", 0.0008);
	}

	@Override
	public Double fetchRateBy(String code, String base) throws ExternalApiGetRequestException {
		if (base.equals("KRW") && rates.containsKey(code)) {
			return rates.get(code);
		} else if (base.equals(code)) {
			return rates.get(base);
		} else {
			throw new ExternalApiGetRequestException("Invalid base or code", HttpStatus.BAD_REQUEST);
		}
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
