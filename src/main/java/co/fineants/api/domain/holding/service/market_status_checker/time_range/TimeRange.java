package co.fineants.api.domain.holding.service.market_status_checker.time_range;

import java.time.LocalTime;

public interface TimeRange {
	boolean isInRange(LocalTime time);
}
