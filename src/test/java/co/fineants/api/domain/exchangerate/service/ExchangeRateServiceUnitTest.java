package co.fineants.api.domain.exchangerate.service;

import static org.assertj.core.groups.Tuple.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.exchangerate.client.ExchangeRateClient;
import co.fineants.api.domain.exchangerate.domain.dto.response.ExchangeRateListResponse;
import co.fineants.api.domain.exchangerate.domain.entity.ExchangeRate;
import co.fineants.api.domain.exchangerate.repository.ExchangeRateRepository;
import co.fineants.api.global.errors.exception.business.ExchangeRateDuplicateException;
import co.fineants.api.global.errors.exception.business.ExchangeRateNotFoundException;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceUnitTest {
	@Mock
	private ExchangeRateRepository repository;
	@Mock
	private ExchangeRateClient exchangeRateClient;
	@Mock
	private ExchangeRateUpdateService exchangeRateUpdateService;
	@InjectMocks
	private ExchangeRateService service;

	@DisplayName("환율 데이터가 추가 및 저장되어야 한다")
	@CsvSource(value = {
		"KRW, 1.0, true",
		"USD, 0.0007322, false",
		"JPY, 0.0097, false",
		"EUR, 0.0088, false",
		"CNY, 0.0122, false",
		"GBP, 0.0077, false",
		"AUD, 0.0088, false",
		"CAD, 0.0088, false",
		"CHF, 0.0088, false"
	})
	@ParameterizedTest
	void should_save_exchange_rate_data(String code, double rate, boolean base) {
		// given
		String baseCode = "KRW";
		BDDMockito.given(repository.findAll())
			.willReturn(Collections.emptyList());
		ExchangeRate baseExchangeRate = ExchangeRate.base(baseCode);
		BDDMockito.given(repository.findBase())
			.willReturn(Optional.of(ExchangeRate.base(baseCode)));
		BDDMockito.given(exchangeRateClient.fetchRateBy(code, baseExchangeRate.getCode()))
			.willReturn(rate);

		// when
		service.createExchangeRate(code);

		// then
		ExchangeRate expected = ExchangeRate.of(code, rate, base);
		BDDMockito.verify(repository, Mockito.times(1)).save(expected);
	}

	@DisplayName("환율 추가 시나리오")
	@TestFactory
	Collection<DynamicTest> createExchangeRateDynamicTest() {
		return List.of(
			DynamicTest.dynamicTest("기준 통화가 없는 상태에서 통화를 추가시 기준 통화가 된다", () -> {
				// given
				String krw = Currency.KRW.name();

				BDDMockito.given(exchangeRateClient.fetchRateBy(krw, krw))
					.willReturn(1.0);
				// when
				service.createExchangeRate(krw);

				// then
				ExchangeRate exchangeRate = ExchangeRate.of(krw, 1.0, true);
				BDDMockito.verify(repository, Mockito.times(1)).save(exchangeRate);

				Assertions.assertThat(exchangeRate)
					.extracting("code", "rate", "base")
					.usingComparatorForType(Percentage::compareTo, Percentage.class)
					.containsExactly(krw, Percentage.from(1.0), true);
			}),
			DynamicTest.dynamicTest("기준 통화가 있는 상태에서 다른 통화를 추가할 수 있다", () -> {
				// given
				String base = "KRW";
				String usd = Currency.USD.name();
				double rate = 0.0007322;
				ExchangeRate baseExchangeRate = ExchangeRate.of(base, 1.0, true);
				BDDMockito.given(repository.findAll())
					.willReturn(List.of(baseExchangeRate));
				BDDMockito.given(repository.findBase())
					.willReturn(Optional.of(baseExchangeRate));
				BDDMockito.given(exchangeRateClient.fetchRateBy(usd, base))
					.willReturn(rate);
				// when
				service.createExchangeRate(usd);

				// then
				ExchangeRate exchangeRate = ExchangeRate.of(usd, rate, false);
				BDDMockito.verify(repository, Mockito.times(1)).save(exchangeRate);
				Assertions.assertThat(exchangeRate)
					.extracting(ExchangeRate::getCode, ExchangeRate::getRate, ExchangeRate::getBase)
					.usingComparatorForType(Percentage::compareTo, Percentage.class)
					.containsExactly(usd, Percentage.from(rate), false);
			})
		);
	}

	@DisplayName("존재하지 않는 통화는 저장할 수 없다")
	@Test
	void should_not_save_exchange_rate_when_not_exist_code_then_throw_exception() {
		// given
		String usd = "AAA";
		BDDMockito.given(exchangeRateClient.fetchRateBy(usd, usd))
			.willThrow(new ExchangeRateNotFoundException(usd));

		// when
		Throwable throwable = Assertions.catchThrowable(() -> service.createExchangeRate(usd));

		// then
		Assertions.assertThat(throwable)
			.isInstanceOf(ExchangeRateNotFoundException.class)
			.hasMessage(usd);
	}

	@DisplayName("이미 존재하는 통화 코드인 경우 예외가 발생해야 한다")
	@Test
	void should_throw_exception_when_code_is_base_code_then_not_save_code() {
		// given
		String usd = "USD";
		ExchangeRate base = ExchangeRate.base(usd);
		BDDMockito.given(repository.findAll())
			.willReturn(List.of(base));

		// when
		Throwable throwable = Assertions.catchThrowable(() -> service.createExchangeRate(usd));

		// then
		Assertions.assertThat(throwable)
			.isInstanceOf(ExchangeRateDuplicateException.class)
			.hasMessage(usd);
	}

	@DisplayName("환율 데이터들을 조회하고자 하면 환율 데이터를 반환되어야 한다")
	@Test
	void should_return_exchange_rate_list_when_read_exchange_rates() {
		// given
		List<ExchangeRate> exchangeRates = List.of(
			ExchangeRate.of("KRW", 1.0, true),
			ExchangeRate.of("USD", 0.1, false)
		);
		BDDMockito.given(repository.findAll())
			.willReturn(exchangeRates);

		// when
		ExchangeRateListResponse response = service.readExchangeRates();
		// then
		Assertions.assertThat(response)
			.extracting("rates")
			.asList()
			.hasSize(2)
			.extracting("code", "rate")
			.usingComparatorForType(Percentage::compareTo, Percentage.class)
			.containsExactlyInAnyOrder(
				tuple("KRW", Percentage.from(1.0)),
				tuple("USD", Percentage.from(0.1))
			);
	}

	@DisplayName("변경하고자 하는 베이스 통화 코드가 usd라면 usd는 기준 통화가 된다")
	@Test
	void should_change_usd_base_is_true_when_base_param_is_usd() {
		// given
		String changeBaseCode = "USD";
		ExchangeRate base = ExchangeRate.base(Currency.KRW.name());
		ExchangeRate usd = ExchangeRate.noneBase(Currency.USD.name(), 0.1);
		BDDMockito.given(repository.findBase())
			.willReturn(Optional.of(base));
		BDDMockito.given(repository.findByCode(changeBaseCode))
			.willReturn(Optional.of(usd));

		// when
		service.patchBase(changeBaseCode);

		// then
		BDDMockito.verify(exchangeRateUpdateService, Mockito.times(1)).updateExchangeRates();
		Assertions.assertThat(base.isBase()).isFalse();
		Assertions.assertThat(usd.isBase()).isTrue();
	}

	// @DisplayName("관리자가 환율을 삭제한다")
	// @Test
	// void deleteExchangeRates() {
	// 	// given
	// 	String krw = "KRW";
	// 	String usd = "USD";
	// 	repository.save(ExchangeRate.base(krw));
	// 	repository.save(ExchangeRate.zero(usd, false));
	//
	// 	// when
	// 	service.deleteExchangeRates(List.of(usd));
	//
	// 	// then
	// 	boolean actual = repository.findByCode(usd).isEmpty();
	// 	assertThat(actual).isTrue();
	// }
	//
	// @DisplayName("관리자는 기준 통화를 제거할 수 없다")
	// @Test
	// void deleteExchangeRates_whenDeletedBaseCode_thenChangeBase() {
	// 	// given
	// 	repository.save(ExchangeRate.base(Currency.KRW.name()));
	// 	repository.save(ExchangeRate.noneBase(Currency.USD.name(), 0.1));
	// 	repository.save(ExchangeRate.noneBase(Currency.CHF.name(), 0.2));
	// 	// when
	// 	Throwable throwable = catchThrowable(() -> service.deleteExchangeRates(List.of(Currency.KRW.name())));
	// 	// then
	// 	assertThat(throwable)
	// 		.isInstanceOf(BaseExchangeRateDeleteInvalidInputException.class)
	// 		.hasMessage(List.of(Currency.KRW.name()).toString());
	// }
	//
	// @DisplayName("관리자가 기준 통화를 제외한 모든 통화를 제거한다")
	// @Test
	// void deleteExchangeRates_whenAllDeleted() {
	// 	// given
	// 	repository.save(ExchangeRate.base(Currency.KRW.name()));
	// 	repository.save(ExchangeRate.noneBase(Currency.USD.name(), 0.1));
	// 	repository.save(ExchangeRate.noneBase(Currency.CHF.name(), 0.2));
	// 	// when
	// 	service.deleteExchangeRates(List.of(Currency.USD.name(), Currency.CHF.name()));
	// 	// then
	// 	List<ExchangeRate> rates = repository.findAll();
	// 	assertThat(rates).hasSize(1);
	// }
	//
	// @DisplayName("USD 통화의 환율 값을 수정한다")
	// @Test
	// void updateRate() {
	// 	// given
	// 	repository.save(ExchangeRate.base(Currency.KRW.name()));
	// 	repository.save(ExchangeRate.noneBase(Currency.USD.name(), 0.1));
	//
	// 	String code = Currency.USD.name();
	// 	double newRate = 0.2;
	// 	// when
	// 	Map<String, Double> actual = service.updateRate(code, newRate);
	// 	// then
	// 	Map<String, Double> expected = Map.of(
	// 		"KRW", 1.0,
	// 		"USD", 0.2
	// 	);
	// 	assertThat(actual)
	// 		.usingRecursiveComparison()
	// 		.isEqualTo(expected);
	// }
}
