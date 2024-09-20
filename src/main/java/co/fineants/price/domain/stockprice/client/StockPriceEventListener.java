package co.fineants.price.domain.stockprice.client;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import co.fineants.price.domain.stockprice.repository.StockPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockPriceEventListener {

	private final StockPriceWebSocketClient client;
	private final StockPriceRepository stockPriceRepository;

	@Async
	@EventListener
	public void onMessage(WebSocketSessionEvent event) {
		log.info("WebSocketSessionEvent : {}", event);
		client.connect();
	}

	@Async
	@EventListener
	public void onMessage(StockPriceDeleteEvent event) {
		log.info("StockPriceDeleteEvent : {}", event);
		stockPriceRepository.remove(event.getTicker());
	}
}