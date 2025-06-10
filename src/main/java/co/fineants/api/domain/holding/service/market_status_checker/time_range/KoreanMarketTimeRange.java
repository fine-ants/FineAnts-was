package co.fineants.api.domain.holding.service.market_status_checker.time_range;

import java.time.LocalTime;

public class KoreanMarketTimeRange implements TimeRange {
	private final LocalTime openTime;
	private final LocalTime closeTime;

	public KoreanMarketTimeRange() {
		openTime = LocalTime.of(9, 0);
		closeTime = LocalTime.of(15, 30);
	}

	@Override
	public boolean isInRange(LocalTime time) {
		return isOpenTime(time) || isCloseTime(time) || isBetween(time);
	}

	private boolean isBetween(LocalTime time) {
		return time.isAfter(openTime) && time.isBefore(closeTime);
	}

	private boolean isOpenTime(LocalTime time) {
		return openTime.equals(time);
	}

	private boolean isCloseTime(LocalTime time) {
		return closeTime.equals(time);
	}
}
