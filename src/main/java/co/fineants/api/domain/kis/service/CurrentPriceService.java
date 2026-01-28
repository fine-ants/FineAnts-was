package co.fineants.api.domain.kis.service;

import java.time.Clock;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.CurrentPriceRedisEntity;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.PriceRepository;
import co.fineants.api.global.errors.exception.business.StockNotFoundException;
import co.fineants.stock.domain.StockRepository;
import co.fineants.stock.event.StockCurrentPriceRefreshEvent;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CurrentPriceService {
	private final PriceRepository priceRepository;
	private final KisService kisService;
	private final Clock clock;
	private final long freshnessThresholdMillis;
	private final ApplicationEventPublisher eventPublisher;
	private final StockRepository stockRepository;
	private final ClosingPriceRepository closingPriceRepository;

	public CurrentPriceService(
		PriceRepository priceRepository,
		KisService kisService,
		Clock clock,
		@Value("${stock.current-price.freshness-threshold-millis:300000}") long freshnessThresholdMillis,
		ApplicationEventPublisher eventPublisher,
		StockRepository stockRepository,
		ClosingPriceRepository closingPriceRepository) {
		this.priceRepository = priceRepository;
		this.kisService = kisService;
		this.clock = clock;
		this.freshnessThresholdMillis = freshnessThresholdMillis;
		this.eventPublisher = eventPublisher;
		this.stockRepository = stockRepository;
		this.closingPriceRepository = closingPriceRepository;
	}

	// TODO: refactoring needed

	/**
	 * 특정 종목의 현재가를 조회한다.
	 *
	 * @param tickerSymbol 티커 심볼
	 * @return 종목 현재가
	 * @throws IllegalStateException 캐시 저장소에 종목의 현재가가 없고 외부(KIS)에서 현재가를 가져오지 못한 경우 발생함
	 */
	public Money fetchPrice(String tickerSymbol) throws IllegalStateException {
		Optional<CurrentPriceRedisEntity> entity = priceRepository.fetchPriceBy(tickerSymbol);

		if (entity.isEmpty()) {
			// 종목 현재가 갱신 이벤트 비동기 발행
			eventPublisher.publishEvent(new StockCurrentPriceRefreshEvent(tickerSymbol));
			// 종목 테이블의 종가 데이터 반환
			return stockRepository.findByTickerSymbol(tickerSymbol)
				.map(stock -> stock.getClosingPrice(closingPriceRepository))
				.orElseThrow(() -> new StockNotFoundException(tickerSymbol))
				.reduce(Bank.getInstance(), Currency.KRW);
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
