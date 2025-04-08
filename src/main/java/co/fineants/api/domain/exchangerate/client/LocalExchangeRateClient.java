package co.fineants.api.domain.exchangerate.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Profile;

import co.fineants.api.global.errors.exception.business.ExternalApiGetRequestException;

@Profile("local")
public class LocalExchangeRateClient implements ExchangeRateClient {

	private static final String BASE = "USD";
	private final Map<String, Double> rates;

	public LocalExchangeRateClient(Map<String, Double> rates) {
		this.rates = rates;
	}

	@Override
	public Double fetchRateBy(String code, String base) throws ExternalApiGetRequestException {
		if (base.equalsIgnoreCase(BASE) || base.equalsIgnoreCase(code)) {
			return rates.get(code);
		}
		double baseRate = rates.get(base);
		double codeRate = rates.get(code);
		return codeRate / baseRate;
	}

	@Override
	public Map<String, Double> fetchRates(String base) {
		Map<String, Double> result = new HashMap<>();
		double baseRate = rates.get(base);
		for (Map.Entry<String, Double> entry : rates.entrySet()) {
			String code = entry.getKey();
			double rate = entry.getValue();
			result.put(code, rate / baseRate);
		}
		return result;
	}
}
