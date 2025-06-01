package co.fineants.api.domain.holding.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;
import co.fineants.api.domain.portfolio.domain.factory.ObserverFactory;
import co.fineants.api.domain.portfolio.domain.factory.PortfolioObserverFactory;
import co.fineants.api.domain.portfolio.reactive.PortfolioObservable;
import co.fineants.api.domain.portfolio.reactive.StockMarketObserver;
import co.fineants.api.global.common.time.LocalDateTimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class PortfolioObservableService {
	private static final long TIMEOUT = 1000L * 40L;
	private final PortfolioObservable portfolioObservable;
	private final StockMarketChecker stockMarketChecker;
	private final LocalDateTimeService localDateTimeService;

	public SseEmitter observePortfolioHoldings(Long portfolioId) {
		SseEmitter emitter = createSseEmitter(portfolioId);

		if (stockMarketChecker.isMarketOpen(localDateTimeService.getLocalDateTimeWithNow())) {
			ObserverFactory<PortfolioHoldingsRealTimeResponse> observerFactory = new PortfolioObserverFactory(3000);
			portfolioObservable.getPortfolioInfo(portfolioId)
				.subscribe(observerFactory.create(emitter));
			return emitter;
		}
		portfolioObservable.getCloseStockMarket()
			.subscribe(StockMarketObserver.create(emitter));
		return emitter;
	}

	private SseEmitter createSseEmitter(Long portfolioId) {
		SseEmitter emitter = new SseEmitter(TIMEOUT);
		emitter.onTimeout(() -> {
			log.info("emitter{} timeout으로 인한 제거", portfolioId);
			emitter.complete();
		});
		emitter.onCompletion(() -> log.info("emitter{} completion으로 인한 제거", portfolioId));
		emitter.onError(throwable -> {
			log.error(throwable.getMessage());
			emitter.completeWithError(throwable);
		});
		return emitter;
	}
}
