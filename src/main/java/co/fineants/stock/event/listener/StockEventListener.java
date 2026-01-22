package co.fineants.stock.event.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import co.fineants.stock.application.ActiveStockService;
import co.fineants.stock.event.StockViewedEvent;
import co.fineants.stock.event.StocksViewedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockEventListener {
	private final ActiveStockService service;

	@EventListener
	@Async
	public void handleStockViewedEvent(StockViewedEvent event) {
		log.info("Handling StockViewedEvent for ticker: {}", event.getTickerSymbol());
		service.markStockAsActive(event.getTickerSymbol());
	}

	@EventListener
	@Async
	public void handleStockViewedEvent(StocksViewedEvent event) {
		log.info("Handling StocksViewedEvent for tickers: {}", event.getTickerSymbols());
		service.markStocksAsActive(event.getTickerSymbols());
	}
}
