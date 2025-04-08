package co.fineants.api.domain.exchangerate.service;

import java.util.List;

import org.apache.logging.log4j.util.Strings;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.exchangerate.client.ExchangeRateClient;
import co.fineants.api.domain.exchangerate.domain.dto.response.ExchangeRateItem;
import co.fineants.api.domain.exchangerate.domain.dto.response.ExchangeRateListResponse;
import co.fineants.api.domain.exchangerate.domain.entity.ExchangeRate;
import co.fineants.api.domain.exchangerate.repository.ExchangeRateRepository;
import co.fineants.api.global.errors.exception.business.BaseExchangeRateDeleteInvalidInputException;
import co.fineants.api.global.errors.exception.business.BaseExchangeRateNotFoundException;
import co.fineants.api.global.errors.exception.business.ExchangeRateDuplicateException;
import co.fineants.api.global.errors.exception.business.ExchangeRateNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

	private final ExchangeRateRepository exchangeRateRepository;
	private final ExchangeRateClient client;
	private final ExchangeRateUpdateService exchangeRateUpdateService;

	@Transactional
	@Secured("ROLE_ADMIN")
	public void createExchangeRate(String code) {
		List<ExchangeRate> rates = exchangeRateRepository.findAll();
		validateDuplicateExchangeRate(rates, code);

		ExchangeRate base = findBaseExchangeRate(code);

		Double rate = client.fetchRateBy(code, base.getCode());
		ExchangeRate exchangeRate = ExchangeRate.of(code, rate, base.equalCode(code));
		exchangeRateRepository.save(exchangeRate);
	}

	private void validateDuplicateExchangeRate(List<ExchangeRate> rates, String code) {
		boolean match = rates.stream()
			.map(ExchangeRate::getCode)
			.anyMatch(c -> c.equals(code));
		if (match) {
			throw new ExchangeRateDuplicateException(code);
		}
	}

	private ExchangeRate findBaseExchangeRate(String defaultCode) {
		return exchangeRateRepository.findBase()
			.orElseGet(() -> ExchangeRate.base(defaultCode));
	}

	@Transactional(readOnly = true)
	@Secured(value = {"ROLE_MANAGER", "ROLE_ADMIN"})
	public ExchangeRateListResponse readExchangeRates() {
		List<ExchangeRateItem> items = exchangeRateRepository.findAll().stream()
			.map(ExchangeRateItem::from)
			.toList();
		return ExchangeRateListResponse.from(items);
	}

	@Transactional
	@Secured("ROLE_ADMIN")
	public void patchBase(String code) {
		// 기존 기준 통화의 base 값을 false로 변경
		findBaseExchangeRate().changeBase(false);
		// code의 base 값을 true로 변경
		findExchangeRateBy(code).changeBase(true);
		exchangeRateUpdateService.updateExchangeRates();
	}

	private ExchangeRate findExchangeRateBy(String code) {
		return exchangeRateRepository.findByCode(code)
			.orElseThrow(() -> new ExchangeRateNotFoundException(code));
	}

	@Transactional
	@Secured("ROLE_ADMIN")
	public void deleteExchangeRates(List<String> codes) {
		validateContainsBaseExchangeRateForDelete(codes);
		exchangeRateRepository.deleteByCodeIn(codes);
	}

	/**
	 * 입력으로 받은 통화 코드들중 기준 통화가 있는지 확인합니다.
	 * @param codes 삭제하고자 하는 통화 코드 리스트
	 */
	private void validateContainsBaseExchangeRateForDelete(List<String> codes) {
		ExchangeRate base = findBaseExchangeRate();
		boolean match = codes.stream()
			.anyMatch(base::equalCode);
		if (match) {
			throw new BaseExchangeRateDeleteInvalidInputException(codes.toString());
		}
	}

	private ExchangeRate findBaseExchangeRate() {
		return exchangeRateRepository.findBase()
			.orElseThrow(() -> new BaseExchangeRateNotFoundException(Strings.EMPTY));
	}
}
