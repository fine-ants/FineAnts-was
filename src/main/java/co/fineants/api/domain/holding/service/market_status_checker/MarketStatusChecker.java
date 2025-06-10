package co.fineants.api.domain.holding.service.market_status_checker;

import java.time.LocalDateTime;

public interface MarketStatusChecker {
	boolean isOpen(LocalDateTime dateTime);

	boolean isClose(LocalDateTime dateTime);
}
