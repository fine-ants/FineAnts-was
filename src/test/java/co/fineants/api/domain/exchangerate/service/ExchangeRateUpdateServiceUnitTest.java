package co.fineants.api.domain.exchangerate.service;

import static org.mockito.BDDMockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.exchangerate.client.ExchangeRateClient;
import co.fineants.api.domain.exchangerate.domain.entity.ExchangeRate;
import co.fineants.api.domain.exchangerate.repository.ExchangeRateRepository;
import co.fineants.api.global.errors.exception.business.BaseExchangeRateNotFoundException;

@ExtendWith(MockitoExtension.class)
class ExchangeRateUpdateServiceUnitTest {

	@Mock
	private ExchangeRateRepository repository;
	@Mock
	private ExchangeRateClient exchangeRateClient;
	@InjectMocks
	private ExchangeRateUpdateService service;

	@DisplayName("KRW 통화 기준으로 USD 통화의 값을 최신화해야 한다")
	@Test
	void should_reload_usd_rate() {
		// given
		ExchangeRate base = ExchangeRate.base(Currency.KRW.name());
		ExchangeRate usd = ExchangeRate.noneBase(Currency.USD.name(), 0.1);
		BDDMockito.given(repository.findAll())
			.willReturn(List.of(base, usd));

		double usdRate = 0.2;
		given(exchangeRateClient.fetchRates(Currency.KRW.name()))
			.willReturn(Map.of(Currency.USD.name(), usdRate));
		// when
		service.updateExchangeRates();
		// then
		Assertions.assertThat(usd.getRate()).isEqualTo(Percentage.from(usdRate));
	}

	@DisplayName("기준 통화가 없으면 환율 최신화가 안된다")
	@Test
	void should_throw_exception_when_not_exist_base_code() {
		// given
		BDDMockito.given(repository.findAll())
			.willReturn(Collections.emptyList());
		// when
		Throwable throwable = Assertions.catchThrowable(() -> service.updateExchangeRates());
		// then
		Assertions.assertThat(throwable)
			.isInstanceOf(BaseExchangeRateNotFoundException.class)
			.hasMessage(Collections.EMPTY_LIST.toString());
	}

	// @DisplayName("외부 API 호출에 실패하면 환율을 업데이트 하지 않는다")
	// @Test
	// void updateExchangeRates_whenExternalApiError_thenNotUpdate() {
	// 	// given
	// 	String krw = "KRW";
	// 	String usd = "USD";
	// 	double rate = 0.1;
	// 	repository.save(ExchangeRate.base(krw));
	// 	repository.save(ExchangeRate.of(usd, rate, false));
	//
	// 	given(exchangeRateClient.fetchRates(krw))
	// 		.willThrow(new ExternalApiGetRequestException("error", HttpStatus.SERVICE_UNAVAILABLE,
	// 			ExchangeRateFetchResponse.requestExceeded().toException()));
	// 	// when
	// 	service.updateExchangeRates();
	// 	// then
	// 	ExchangeRate actual = repository.findByCode(usd).orElseThrow();
	// 	assertThat(actual.getRate().toDoubleValue()).isEqualTo(rate);
	// }
}
