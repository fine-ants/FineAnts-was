package co.fineants.api.domain.exchangerate.scheduler;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.exchangerate.service.ExchangeRateUpdateService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile(value = {"production", "release"})
public class ExchangeRateScheduler {

	private final ExchangeRateUpdateService service;

	@Scheduled(cron = "0 0 * * * *") // 매일 자정에 한번씩 수행
	@Transactional
	public void updateExchangeRates() {
		service.updateExchangeRates();
	}
}
