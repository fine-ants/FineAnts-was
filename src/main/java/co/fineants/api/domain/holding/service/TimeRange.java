package co.fineants.api.domain.holding.service;

import java.time.LocalTime;

public interface TimeRange {
	boolean isInRange(LocalTime time);
}
