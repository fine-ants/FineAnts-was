package co.fineants.api.domain.holding.service;

import java.time.LocalDateTime;

public class TimeMarketStatusChecker implements MarketStatusChecker {

	private final TimeRange timeRange;

	public TimeMarketStatusChecker(TimeRange timeRange) {
		this.timeRange = timeRange;
	}

	@Override
	public boolean isOpen(LocalDateTime dateTime) {
		return timeRange.isInRange(dateTime.toLocalTime());
	}
}
