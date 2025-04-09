package co.fineants.api.domain.exchangerate.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.exchangerate.client.ExchangeRateClient;
import co.fineants.api.domain.exchangerate.domain.entity.ExchangeRate;
import co.fineants.api.domain.exchangerate.repository.ExchangeRateRepository;
import co.fineants.api.global.errors.exception.business.BaseExchangeRateNotFoundException;
import co.fineants.api.global.errors.exception.business.ExternalApiGetRequestException;
import co.fineants.api.infra.mail.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateUpdateService {

	private final ExchangeRateRepository exchangeRateRepository;
	private final ExchangeRateClient client;
	private final EmailService emailService;

	@Transactional
	public void updateExchangeRates() throws BaseExchangeRateNotFoundException {
		List<ExchangeRate> originalRates = exchangeRateRepository.findAll();
		validateExistBase(originalRates);
		ExchangeRate baseRate = findBaseExchangeRate(originalRates);
		Map<String, Double> rateMap;
		try {
			rateMap = client.fetchRates(baseRate.getCode());
		} catch (ExternalApiGetRequestException e) {
			log.warn("ExchangeRateUpdateService updateExchangeRates error", e);
			// send mail to admin
			emailService.sendExchangeRateErrorEmail();
			rateMap = Collections.emptyMap();
		}
		Map<String, Double> finalRateMap = rateMap;
		originalRates.stream()
			.filter(rate -> finalRateMap.containsKey(rate.getCode()))
			.forEach(rate -> rate.changeRate(finalRateMap.get(rate.getCode())));
	}

	private void validateExistBase(List<ExchangeRate> rates) throws BaseExchangeRateNotFoundException {
		if (rates.stream()
			.noneMatch(ExchangeRate::isBase)) {
			throw new BaseExchangeRateNotFoundException(rates.toString());
		}
	}

	private ExchangeRate findBaseExchangeRate(List<ExchangeRate> rates) {
		return rates.stream()
			.filter(ExchangeRate::isBase)
			.findFirst()
			.orElseThrow(() -> new BaseExchangeRateNotFoundException(rates.toString()));
	}
}
