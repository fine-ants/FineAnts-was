package co.fineants.api.domain.kis.service;

import java.time.Clock;
import java.util.Optional;

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
		@Value("${stock.current-price.freshness-threshold-millis:300000}") long freshnessThresholdMillis,
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
			log.warn("Price not found in cache for tickerSymbol={}. Returning closing price instead.", tickerSymbol);
			return findStock.byTickerSymbol(tickerSymbol)
				.getClosingPrice(closingPriceRepository)
				.reduce(Bank.getInstance(), Currency.KRW);
		}
		// 신선도가 낮은 경우 외부 API에서 다시 가져와 저장
		if (!entity.get().isFresh(clock.millis(), freshnessThresholdMillis)) {
			// 종목 현재가 갱신 이벤트 비동기 발행
			eventPublisher.publishEvent(new StockCurrentPriceRefreshEvent(tickerSymbol));
			// 기존 신선도 낮은 데이터 반환
			log.warn("Fetched stale price for entity={}", entity.get());
			return entity.get().getPriceMoney();
		}
		return entity.get().getPriceMoney();
	}
}
