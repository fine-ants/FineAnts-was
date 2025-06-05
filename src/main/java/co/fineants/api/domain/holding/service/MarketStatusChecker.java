package co.fineants.api.domain.holding.service;

import java.time.LocalDateTime;

public interface MarketStatusChecker {
	boolean isOpen(LocalDateTime dateTime);

	default boolean isClose(LocalDateTime dateTime) {
		return !isOpen(dateTime);
	}
}
