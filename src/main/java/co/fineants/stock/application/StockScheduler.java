package co.fineants.stock.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import co.fineants.stock.domain.event.StockReloadEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StockScheduler {

	private final ReloadStock reloadStock;
	private final ApplicationEventPublisher publisher;

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
}
