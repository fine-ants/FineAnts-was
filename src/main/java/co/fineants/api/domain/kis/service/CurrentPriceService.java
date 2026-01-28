package co.fineants.api.domain.kis.service;

import java.time.Clock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.kis.domain.CurrentPriceRedisEntity;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.PriceRepository;
import co.fineants.stock.application.FindStock;
import co.fineants.stock.event.StockCurrentPriceRefreshEvent;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CurrentPriceService {
	private final PriceRepository priceRepository;
	private final Clock clock;
	private final long freshnessThresholdMillis;
	private final ApplicationEventPublisher eventPublisher;
	private final ClosingPriceRepository closingPriceRepository;
	private final FindStock findStock;

	public CurrentPriceService(
		PriceRepository priceRepository,
		Clock clock,
		@Value("${stock.current-price.freshness-threshold-millis:5000}") long freshnessThresholdMillis,
		ApplicationEventPublisher eventPublisher,
		ClosingPriceRepository closingPriceRepository,
		FindStock findStock) {
		this.priceRepository = priceRepository;
		this.clock = clock;
		this.freshnessThresholdMillis = freshnessThresholdMillis;
		this.eventPublisher = eventPublisher;
		this.closingPriceRepository = closingPriceRepository;
		this.findStock = findStock;
	}

	/**
	 * 특정 종목의 현재가를 조회한다.
	 *
	 * @param tickerSymbol 티커 심볼
	 * @return 종목 현재가
	 */
	public Money fetchPrice(String tickerSymbol) {
		return priceRepository.fetchPriceBy(tickerSymbol)
			.map(this::processCachedEntity)
			.orElseGet(() -> handleCacheMiss(tickerSymbol));
	}

	private Money processCachedEntity(CurrentPriceRedisEntity entity) {
		// 신선하지 않다면 비동기 갱신 트리거 (Stale-While-Revalidate)
		if (isStale(entity)) {
			log.warn("Stale price detected for {}. Triggering refresh.", entity.getTickerSymbol());
			triggerRefresh(entity.getTickerSymbol());
		}
		return entity.getPriceMoney();
	}

	private boolean isStale(CurrentPriceRedisEntity entity) {
		return !entity.isFresh(clock.millis(), freshnessThresholdMillis);
	}

	private void triggerRefresh(String tickerSymbol) {
		eventPublisher.publishEvent(new StockCurrentPriceRefreshEvent(tickerSymbol));
	}

	private Money handleCacheMiss(String tickerSymbol) {
		log.warn("Cache miss for {}. Triggering refresh and returning fallback price.", tickerSymbol);
		triggerRefresh(tickerSymbol);
		return getFallbackPrice(tickerSymbol);
	}

	private Money getFallbackPrice(String tickerSymbol) {
		return findStock.byTickerSymbol(tickerSymbol)
			.getClosingPrice(closingPriceRepository)
			.reduce(Bank.getInstance(), Currency.KRW);
	}
}
