package co.fineants.api.domain.holding.service;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class TimeMarketStatusChecker implements MarketStatusChecker {

	private final LocalTime openTime;
	private final LocalTime closeTime;

	public TimeMarketStatusChecker() {
		openTime = LocalTime.of(9, 0);
		closeTime = LocalTime.of(15, 30);
	}

	@Override
	public boolean isOpen(LocalDateTime dateTime) {
		LocalTime time = dateTime.toLocalTime();
		if (openTime.equals(time)) {
			return true;
		}
		if (closeTime.equals(time)) {
			return true;
		}
		return time.isAfter(openTime) && time.isBefore(closeTime);
	}
}
