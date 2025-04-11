package co.fineants.api.domain.exchangerate.scheduler;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import co.fineants.api.domain.exchangerate.service.ExchangeRateUpdateService;
import co.fineants.api.global.errors.exception.business.BaseExchangeRateNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Profile(value = {"production", "release"})
@Slf4j
public class ExchangeRateScheduler {

	private final ExchangeRateUpdateService service;

	/**
	 * 매일 00시 0분 0초에 한번씩 환율을 업데이트합니다.
	 */
	@SchedulerLock(name = "exchangeRateScheduler", lockAtLeastFor = "1m", lockAtMostFor = "1m")
	@Scheduled(cron = "0 0 * * * *")
	@Transactional
	public void scheduledUpdateExchangeRates() {
		try {
			service.updateExchangeRates();
		} catch (BaseExchangeRateNotFoundException e) {
			log.warn("환율 업데이트 실패", e);
		}
	}
}
