package co.fineants.api.domain.portfolio.domain.factory;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;
import co.fineants.api.domain.portfolio.reactive.PortfolioObserver;
import io.reactivex.rxjava3.core.Observer;

public class PortfolioObserverFactory implements ObserverFactory<PortfolioHoldingsRealTimeResponse> {

	private final long reconnectTimeMillis;

	public PortfolioObserverFactory(long reconnectTimeMillis) {
		this.reconnectTimeMillis = reconnectTimeMillis;
	}

	@Override
	public Observer<PortfolioHoldingsRealTimeResponse> create(SseEmitter emitter) {
		return new PortfolioObserver(emitter, reconnectTimeMillis);
	}
}
