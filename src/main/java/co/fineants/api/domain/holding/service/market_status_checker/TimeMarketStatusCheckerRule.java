package co.fineants.api.domain.holding.service.market_status_checker;

import java.time.LocalDateTime;

import co.fineants.api.domain.holding.service.market_status_checker.time_range.TimeRange;

public class TimeMarketStatusCheckerRule implements MarketStatusCheckerRule {

	private final TimeRange timeRange;

	public TimeMarketStatusCheckerRule(TimeRange timeRange) {
		this.timeRange = timeRange;
	}

	@Override
	public boolean isOpen(LocalDateTime dateTime) {
		return timeRange.isInRange(dateTime.toLocalTime());
	}
}
