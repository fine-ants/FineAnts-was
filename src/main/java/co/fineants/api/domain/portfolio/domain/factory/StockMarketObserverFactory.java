package co.fineants.api.domain.portfolio.domain.factory;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.portfolio.reactive.StockMarketObserver;
import io.reactivex.rxjava3.core.Observer;

public class StockMarketObserverFactory implements ObserverFactory<String> {

	private final long reconnectTimeMillis;

	public StockMarketObserverFactory(long reconnectTimeMillis) {
		this.reconnectTimeMillis = reconnectTimeMillis;
	}

	@Override
	public Observer<String> create(SseEmitter emitter) {
		return new StockMarketObserver(emitter, reconnectTimeMillis);
	}
}
