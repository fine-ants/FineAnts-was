package co.fineants.api.domain.exchangerate.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Profile;

@Profile("local")
public class LocalExchangeRateClient implements ExchangeRateClient {

	private final Map<String, Double> rates;

	public LocalExchangeRateClient(Map<String, Double> rates) {
		this.rates = rates;
	}

	@Override
	public Double fetchRateBy(String code, String base) {
		double codeRate = rates.get(code.toUpperCase());
		double baseRate = rates.get(base.toUpperCase());
		return codeRate / baseRate;
	}

	@Override
	public Map<String, Double> fetchRates(String base) {
		Map<String, Double> result = new HashMap<>();
		Double baseRate = rates.get(base);
		for (Map.Entry<String, Double> entry : rates.entrySet()) {
			String code = entry.getKey();
			Double rate = entry.getValue();
			result.put(code, rate / baseRate);
		}
		return result;
	}
}
