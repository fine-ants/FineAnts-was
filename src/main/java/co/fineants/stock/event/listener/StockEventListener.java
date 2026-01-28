package co.fineants.stock.event.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import co.fineants.api.domain.kis.repository.PriceRepository;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.stock.application.ActiveStockService;
import co.fineants.stock.event.StockCurrentPriceRefreshEvent;
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
	private final PriceRepository priceRepository;

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
			.doOnSuccess(kisCurrentPrice -> log.info("Fetched current price from KIS - {}", kisCurrentPrice))
			.subscribe(kisCurrentPrice ->
				priceRepository.savePrice(kisCurrentPrice.getTickerSymbol(), kisCurrentPrice.getPrice()));
	}
}
