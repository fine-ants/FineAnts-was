package co.fineants.api.domain.kis.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.repository.PriceRepository;
import co.fineants.api.global.common.delay.DelayManager;
import co.fineants.api.global.errors.exception.business.CredentialsTypeKisException;
import co.fineants.api.global.errors.exception.business.ExpiredAccessTokenKisException;
import co.fineants.api.global.errors.exception.business.RequestLimitExceededKisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrentPriceService {
	private final PriceRepository priceRepository;
	private final KisClient kisClient;
	private final DelayManager delayManager;

	public Money fetchPrice(String tickerSymbol) {
		Optional<Money> money = priceRepository.fetchPriceBy(tickerSymbol);

		if (money.isEmpty()) {
			Optional<Long> freshPrice = fetchPriceFromKis(tickerSymbol);
			if (freshPrice.isPresent()) {
				priceRepository.savePrice(tickerSymbol, freshPrice.get());
				return Money.won(freshPrice.get());
			}
			throw new IllegalStateException("현재가를 가져올 수 없습니다. tickerSymbol=" + tickerSymbol);
		}
		return money.get();
	}

	private Optional<Long> fetchPriceFromKis(String tickerSymbol) {
		return kisClient.fetchCurrentPrice(tickerSymbol)
			.doOnSuccess(price -> log.debug("reload stock current price {}", price))
			.onErrorResume(ExpiredAccessTokenKisException.class::isInstance, throwable -> Mono.empty())
			.onErrorResume(CredentialsTypeKisException.class::isInstance, throwable -> Mono.empty())
			.retryWhen(Retry.fixedDelay(5, delayManager.fixedDelay())
				.filter(RequestLimitExceededKisException.class::isInstance))
			.onErrorResume(Exceptions::isRetryExhausted, throwable -> Mono.empty())
			.blockOptional(delayManager.timeout())
			.map(KisCurrentPrice::getPrice);
	}
}
