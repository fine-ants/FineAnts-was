package co.fineants.api.domain.holding.service.market_status_checker;

import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.api.domain.holding.service.market_status_checker.time_range.KoreanMarketTimeRange;
import co.fineants.api.domain.holding.service.market_status_checker.time_range.TimeRange;

class KoreanMarketStatusCheckerTest {

	@DisplayName("정규시간 내에서는 true를 반환한다.")
	@Test
	void shouldReturnTrue_whenDateTimeIsInRegularTime() {
		// given
		TimeRange regularTimeRange = new KoreanMarketTimeRange();
		MarketStatusCheckerRule rule = new TimeMarketStatusCheckerRule(regularTimeRange);
		MarketStatusChecker checker = new KoreanMarketStatusChecker(rule);
		LocalDateTime dateTime = LocalDateTime.of(2025, 6, 10, 9, 0);
		// when
		boolean isOpen = checker.isOpen(dateTime);
		// then
		Assertions.assertThat(isOpen).isTrue();
	}

	// todo: 규칙이 아무것도 없으면 false를 반환해야 한다
}
