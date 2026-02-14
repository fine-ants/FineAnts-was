package co.fineants.stock.application;

import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import co.fineants.api.domain.kis.service.CurrentPriceService;
import co.fineants.stock.domain.event.StockReloadEvent;
import co.fineants.stock.event.StockCurrentPriceRequiredEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StockScheduler {

	private final ReloadStock reloadStock;
	private final ApplicationEventPublisher publisher;
	private final CurrentPriceService currentPriceService;

	/**
	 * 매일 오전 8시에 주식 정보를 업데이트합니다.
	 */
	@SchedulerLock(name = "stockScheduler", lockAtLeastFor = "1m", lockAtMostFor = "1m")
	@Scheduled(cron = "${cron.expression.reload-stocks:0 0 8 * * ?}")
	@Transactional
	public void scheduledReloadStocks() {
		reloadStock.reloadStocks();
		publisher.publishEvent(new StockReloadEvent());
	}

	/**
	 * 평일 15:30에 캐시 저장소에 저장된 종목의 현재가를 최신 정보로 갱신합니다.
	 */
	@SchedulerLock(name = "batchStockCurrentPriceRefreshScheduler", lockAtLeastFor = "1m", lockAtMostFor = "1m")
	@Scheduled(cron = "0 30 15 ? * MON-FRI")
	public void scheduledRefreshCurrentPrice() {
		// redis에 저장된 현재가 종목들을 가져옵니다.
		Set<String> tickers = currentPriceService.getAllTickers();
		// 각 종목의 현재가를 갱신하는 동기 이벤트를 발행합니다.
		tickers.forEach(ticker -> publisher.publishEvent(new StockCurrentPriceRequiredEvent(ticker)));
	}
}
