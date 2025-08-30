package co.fineants.api.domain.holding.service.market_status_checker;

import java.time.LocalDateTime;

public interface MarketStatusCheckerRule {
	boolean isOpen(LocalDateTime dateTime);

	default boolean isClose(LocalDateTime dateTime) {
		return !isOpen(dateTime);
	}
}
