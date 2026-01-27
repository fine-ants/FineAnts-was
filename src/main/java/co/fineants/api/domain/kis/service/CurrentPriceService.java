package co.fineants.api.domain.kis.service;

import java.time.Clock;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.CurrentPriceRedisEntity;
import co.fineants.api.domain.kis.repository.PriceRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CurrentPriceService {
	private final PriceRepository priceRepository;
	private final KisService kisService;
	private final Clock clock;
	private final long freshnessThresholdMillis;

	public CurrentPriceService(
		PriceRepository priceRepository,
		KisService kisService,
		Clock clock,
		@Value("${stock.current-price.freshness-threshold-millis:300000}") long freshnessThresholdMillis) {
		this.priceRepository = priceRepository;
		this.kisService = kisService;
		this.clock = clock;
		this.freshnessThresholdMillis = freshnessThresholdMillis;
	}

	// TODO: refactoring needed

	/**
	 * 특정 종목의 현재가를 조회한다.
	 * @param tickerSymbol 티커 심볼
	 * @return 종목 현재가
	 * @throws IllegalStateException 캐시 저장소에 종목의 현재가가 없는 경우 발생함
	 */
	public Money fetchPrice(String tickerSymbol) throws IllegalStateException {
		Optional<CurrentPriceRedisEntity> entity = priceRepository.fetchPriceBy(tickerSymbol);

		if (entity.isEmpty()) {
			Optional<Long> freshPrice = fetchPriceFromKis(tickerSymbol);
			if (freshPrice.isPresent()) {
				priceRepository.savePrice(tickerSymbol, freshPrice.get());
				return Money.won(freshPrice.get());
			}
			throw new IllegalStateException("현재가를 가져올 수 없습니다. tickerSymbol=" + tickerSymbol);
		}
		// 신선도가 낮은 경우 외부 API에서 다시 가져와 저장
		if (!entity.get().isFresh(clock.millis(), freshnessThresholdMillis)) {
			Optional<Long> freshPrice = fetchPriceFromKis(tickerSymbol);
			if (freshPrice.isPresent()) {
				priceRepository.savePrice(tickerSymbol, freshPrice.get());
				return Money.won(freshPrice.get());
			}
			log.warn("신선한 현재가를 가져올 수 없습니다. tickerSymbol={}", tickerSymbol);
		}
		return entity.get().getPriceMoney();
	}

	private Optional<Long> fetchPriceFromKis(String tickerSymbol) {
		return kisService.fetchCurrentPrice(tickerSymbol)
			.map(KisCurrentPrice::getPrice)
			.blockOptional();
	}
}
