package co.fineants.stock.event.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.dto.response.KisClosingPrice;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.service.CurrentPriceService;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.api.global.common.delay.DelayManager;
import co.fineants.stock.application.ActiveStockService;
import co.fineants.stock.event.StockClosingPriceRefreshEvent;
import co.fineants.stock.event.StockClosingPriceRequiredEvent;
import co.fineants.stock.event.StockCurrentPriceRefreshEvent;
import co.fineants.stock.event.StockCurrentPriceRequiredEvent;
import co.fineants.stock.event.StockViewedEvent;
import co.fineants.stock.event.StocksViewedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockEventListener {
	private final ActiveStockService service;
	private final KisService kisService;
	private final CurrentPriceService currentPriceService;
	private final ClosingPriceRepository closingPriceRepository;
	private final DelayManager delayManager;

	@EventListener
	@Async
	public void handleStockViewedEvent(StockViewedEvent event) {
		log.info("Handling StockViewedEvent for ticker: {}", event.getTickerSymbol());
		service.markStockAsActive(event.getTickerSymbol());
	}

	@EventListener
	@Async
	public void handleStocksViewedEvent(StocksViewedEvent event) {
		log.info("Handling StocksViewedEvent for tickers: {}", event.getTickerSymbols());
		service.markStocksAsActive(event.getTickerSymbols());
	}

	@EventListener
	@Async
	public void handleStockCurrentPriceRefreshEvent(StockCurrentPriceRefreshEvent event) {
		log.info("Handling StockCurrentPriceRefreshEvent - tickerSymbol={}", event.getTickerSymbol());
		kisService.fetchCurrentPrice(event.getTickerSymbol())
			.doOnSuccess(this::logCurrentPrice)
			.subscribe(this::savePrice,
				error -> log.warn("Warning fetching current price for ticker: {}", event.getTickerSymbol(), error)
			);
	}

	private void logCurrentPrice(KisCurrentPrice kisCurrentPrice) {
		log.info("Fetched current price from KIS - {}", kisCurrentPrice);
	}

	@EventListener
	public void handleStockCurrentPriceRequiredEvent(StockCurrentPriceRequiredEvent event) {
		log.info("Handling StockCurrentPriceRequiredEvent - tickerSymbol={}", event.getTickerSymbol());
		kisService.fetchCurrentPrice(event.getTickerSymbol())
			.doOnSuccess(this::logCurrentPrice)
			.blockOptional(delayManager.timeout())
			.ifPresent(this::savePrice);
	}

	private void savePrice(KisCurrentPrice price) {
		currentPriceService.savePrice(price.getTickerSymbol(), price.getPrice());
	}

	@EventListener
	@Async
	public void handleStockClosingPriceRefreshEvent(StockClosingPriceRefreshEvent event) {
		log.info("Handling StockClosingPriceRefreshEvent - tickerSymbol={}", event.getTickerSymbol());
		kisService.fetchClosingPrice(event.getTickerSymbol())
			.doOnSuccess(kisClosingPrice -> log.info("Fetched closing price from KIS - {}", kisClosingPrice))
			.subscribe(this::saveClosingPrice,
				error -> log.warn("Warning fetching closing price for ticker: {}", event.getTickerSymbol(), error)
			);
	}

	private void saveClosingPrice(KisClosingPrice price) {
		closingPriceRepository.savePrice(price.getTickerSymbol(), price.getPrice());
	}

	@EventListener
	public void handleStockClosingPriceRequiredEvent(StockClosingPriceRequiredEvent event) {
		log.info("Handling StockClosingPriceRequiredEvent - tickerSymbol={}", event.getTickerSymbol());
		kisService.fetchClosingPrice(event.getTickerSymbol())
			.doOnSuccess(kisClosingPrice -> log.info("Fetched closing price from KIS - {}", kisClosingPrice))
			.blockOptional(delayManager.timeout())
			.ifPresent(this::saveClosingPrice);
	}
}
