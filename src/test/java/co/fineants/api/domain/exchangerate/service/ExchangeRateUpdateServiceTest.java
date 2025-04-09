package co.fineants.api.domain.exchangerate.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.exchangerate.client.ExchangeRateClient;
import co.fineants.api.domain.exchangerate.domain.entity.ExchangeRate;
import co.fineants.api.domain.exchangerate.repository.ExchangeRateRepository;
import co.fineants.api.global.errors.exception.business.BaseExchangeRateNotFoundException;
import co.fineants.api.global.errors.exception.business.ExternalApiGetRequestException;

class ExchangeRateUpdateServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private ExchangeRateUpdateService service;
	@Autowired
	private ExchangeRateRepository repository;
	@Autowired
	private ExchangeRateClient mockedExchangeRateClient;

	@Transactional
	@DisplayName("환율을 최신화한다")
	@Test
	void updateExchangeRates() {
		// given
		String krw = "KRW";
		String usd = "USD";
		double rate = 0.1;
		repository.save(ExchangeRate.base(krw));
		repository.save(ExchangeRate.of(usd, rate, false));

		double usdRate = 0.2;
		given(mockedExchangeRateClient.fetchRates(krw)).willReturn(Map.of(usd, usdRate));
		// when
		service.updateExchangeRates();
		// then
		ExchangeRate exchangeRate = repository.findByCode(usd).orElseThrow();
		Percentage expected = Percentage.from(usdRate);
		assertThat(exchangeRate)
			.extracting("rate")
			.usingComparatorForType(Percentage::compareTo, Percentage.class)
			.isEqualTo(expected);
	}

	@DisplayName("관리자는 기준 통화가 없는 상태에서 환율 업데이트를 할 수 없다")
	@Test
	void updateExchangeRates_whenNoBase_thenError() {
		// given
		String baseCode = "KRW";
		given(mockedExchangeRateClient.fetchRates(baseCode)).willReturn(Map.of(baseCode, 1.0));
		// when
		Throwable throwable = catchThrowable(() -> service.updateExchangeRates());
		// then
		assertThat(throwable)
			.isInstanceOf(BaseExchangeRateNotFoundException.class)
			.hasMessage(Collections.EMPTY_LIST.toString());
	}

	@DisplayName("외부 API 호출에 실패하면 환율을 업데이트 하지 않는다")
	@Test
	void updateExchangeRates_whenExternalApiError_thenNotUpdate() {
		// given
		String krw = "KRW";
		String usd = "USD";
		double rate = 0.1;
		repository.save(ExchangeRate.base(krw));
		repository.save(ExchangeRate.of(usd, rate, false));

		given(mockedExchangeRateClient.fetchRates(krw))
			.willThrow(ExternalApiGetRequestException.class);
		// when
		service.updateExchangeRates();
		// then
		ExchangeRate actual = repository.findByCode(usd).orElseThrow();
		assertThat(actual.getRate().toDoubleValue()).isEqualTo(rate);
	}
}
