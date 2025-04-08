package co.fineants.api.domain.exchangerate.domain.dto.response;

import java.util.Map;

public class ExchangeRateUpdateResponse {
	private final Map<String, Double> rates;

	public ExchangeRateUpdateResponse(Map<String, Double> rates) {
		this.rates = rates;
	}

	@Override
	public String toString() {
		return String.format("ExchangeRateUpdateResponse{rates=%s}", rates);
	}
}
