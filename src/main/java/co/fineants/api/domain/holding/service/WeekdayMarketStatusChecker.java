package co.fineants.api.domain.holding.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

@Component
public class WeekdayMarketStatusChecker implements MarketStatusChecker {

	@Override
	public boolean isOpen(LocalDateTime dateTime) {
		DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
		return !isWeekend(dayOfWeek);
	}

	private boolean isWeekend(DayOfWeek dayOfWeek) {
		return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
	}
}
