package co.fineants.api.domain.exchangerate.scheduler;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import co.fineants.api.domain.exchangerate.service.ExchangeRateUpdateService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile(value = {"production", "release"})
public class ExchangeRateScheduler {

	private final ExchangeRateUpdateService service;

	/**
	 * 매일 0시 0분에 환율을 업데이트합니다.
	 */
	@SchedulerLock(name = "exchangeRateScheduler")
	@Scheduled(cron = "0 0 * * * *")
	@Transactional
	public void updateExchangeRates() {
		service.updateExchangeRates();
	}
}
