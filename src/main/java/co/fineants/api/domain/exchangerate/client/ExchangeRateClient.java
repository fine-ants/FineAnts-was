package co.fineants.api.domain.exchangerate.client;

import java.util.Map;

import co.fineants.api.global.errors.exception.business.ExternalApiGetRequestException;

public interface ExchangeRateClient {
	Double fetchRateBy(String code, String base) throws ExternalApiGetRequestException;

	Map<String, Double> fetchRates(String base);
}
