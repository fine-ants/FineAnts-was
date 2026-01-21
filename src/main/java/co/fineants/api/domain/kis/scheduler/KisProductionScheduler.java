package co.fineants.api.domain.kis.scheduler;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import co.fineants.api.domain.holiday.service.HolidayService;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.stock.application.ActiveStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile(value = {"production", "kis-scheduler"})
@Slf4j
@RequiredArgsConstructor
@Service
public class KisProductionScheduler {

	private final HolidayService holidayService;
	private final KisService kisService;
	private final ActiveStockService activeStockService;

	/**
	 * 평일 09:00~16:00 시간 동안 5초 간격으로 KIS에서 주식 현재가를 업데이트합니다.
	 * <p>
	 * 휴장일 및 주말에는 실행하지 않습니다.
	 * </p>
	 */
	@SchedulerLock(name = "kisCurrentPriceScheduler", lockAtLeastFor = "6s", lockAtMostFor = "12s")
	@Scheduled(cron = "0/5 * 9-16 ? * MON,TUE,WED,THU,FRI")
	@Transactional
	public void refreshCurrentPrice() {
		if (holidayService.isHoliday(LocalDate.now())) {
			return;
		}
		Set<String> activeTickerSymbols = activeStockService.getActiveStockTickerSymbols(5);
		if (activeTickerSymbols.isEmpty()) {
			log.info("No active stocks in the last 5 minutes. Skipping KIS current price refresh.");
			return;
		}
		List<KisCurrentPrice> prices = kisService.refreshAllStockCurrentPrice(activeTickerSymbols);
		log.info("The stock's current price has renewed {} out of {}", prices.size(), activeTickerSymbols.size());
	}

	/**
	 * 매 시간마다 활동이 없는 종목 데이터 정리
	 */
	@SchedulerLock(name = "kisCleanupInactiveStocksScheduler", lockAtLeastFor = "50s", lockAtMostFor = "110s")
	@Scheduled(cron = "0 0 * * * *")
	public void cleanup() {
		activeStockService.cleanupInactiveStocks(60);
	}
}
