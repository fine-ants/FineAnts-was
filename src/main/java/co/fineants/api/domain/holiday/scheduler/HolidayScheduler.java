package co.fineants.api.domain.holiday.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import co.fineants.api.domain.holiday.domain.entity.Holiday;
import co.fineants.api.domain.holiday.service.HolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class HolidayScheduler {
	private final HolidayService service;

	/**
	 * 정오(0시 1분 0초)에 한번씩 국내 휴장 일정을 업데이트한다
	 */
	@SchedulerLock(name = "holidayScheduler", lockAtLeastFor = "1m", lockAtMostFor = "1m")
	@Scheduled(cron = "${cron.expression.update-holidays:0 0 0 * * ?}")
	@Transactional
	public void updateHolidays() {
		List<Holiday> holidays = service.updateHoliday(LocalDate.now());
		log.info("update holidays : {}", holidays);
	}
}
