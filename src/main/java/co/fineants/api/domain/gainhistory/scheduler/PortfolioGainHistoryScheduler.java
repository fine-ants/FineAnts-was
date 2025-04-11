package co.fineants.api.domain.gainhistory.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import co.fineants.api.domain.gainhistory.domain.dto.response.PortfolioGainHistoryCreateResponse;
import co.fineants.api.domain.gainhistory.service.PortfolioGainHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioGainHistoryScheduler {

	private final PortfolioGainHistoryService service;

	/**
	 * 매일 16시에 포트폴리오 수익 내역을 기록합니다.
	 * <p>
	 * 실행이 완료된 후에도 1분 동안 lock을 유지합니다.
	 * </p>
	 */
	@SchedulerLock(name = "portfolioGainHistoryScheduler", lockAtLeastFor = "1m", lockAtMostFor = "1m")
	@Scheduled(cron = "0 0 16 * * ?") // 매일 16시에 실행
	@Transactional
	public void scheduledPortfolioGainHistory() {
		PortfolioGainHistoryCreateResponse response = service.addPortfolioGainHistory();
		log.info("포트폴리오 수익 내역 기록 결과, size = {}", response.getIds().size());
	}
}
