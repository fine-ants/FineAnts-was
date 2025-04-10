package co.fineants.api.domain.exchangerate.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.exchangerate.client.ExchangeRateClient;
import co.fineants.api.domain.exchangerate.domain.entity.ExchangeRate;
import co.fineants.api.domain.exchangerate.repository.ExchangeRateRepository;
import co.fineants.api.global.errors.exception.business.BaseExchangeRateNotFoundException;
import co.fineants.api.global.errors.exception.business.ExternalApiGetRequestException;
import co.fineants.api.infra.mail.EmailService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ExchangeRateUpdateService {

	private final ExchangeRateRepository exchangeRateRepository;
	private final ExchangeRateClient client;
	private final EmailService emailService;
	private final String adminEmail;

	public ExchangeRateUpdateService(ExchangeRateRepository exchangeRateRepository, ExchangeRateClient client,
		EmailService emailService, @Value("${admin.email}") String adminEmail) {
		this.exchangeRateRepository = exchangeRateRepository;
		this.client = client;
		this.emailService = emailService;
		this.adminEmail = adminEmail;
	}

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
			sendExchangeRateErrorNotification(e);
			rateMap = Collections.emptyMap();
		}
		Map<String, Double> finalRateMap = rateMap;
		originalRates.stream()
			.filter(rate -> finalRateMap.containsKey(rate.getCode()))
			.forEach(rate -> rate.changeRate(finalRateMap.get(rate.getCode())));
	}

	private void sendExchangeRateErrorNotification(ExternalApiGetRequestException e) {
		String subject = "환율 API 서버 오류";
		String templateName = "mail-templates/exchange-rate-fail-notification_template";
		String apiUrl = "https://exchange-rate-api1.p.rapidapi.com";
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String stackTrace = sw.toString();
		Map<String, String> values = Map.of(
			"failedAt", LocalDateTime.now().toString(),
			"apiUrl", apiUrl,
			"errorMessage", e.getErrorCodeMessage(),
			"stackTrace", stackTrace
		);
		emailService.sendEmail(adminEmail, subject, templateName, values);
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
