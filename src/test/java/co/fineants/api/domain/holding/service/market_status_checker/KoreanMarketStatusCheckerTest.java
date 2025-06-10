package co.fineants.api.domain.holding.service.market_status_checker;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import co.fineants.api.domain.holding.service.market_status_checker.time_range.KoreanMarketTimeRange;
import co.fineants.api.domain.holding.service.market_status_checker.time_range.TimeRange;

class KoreanMarketStatusCheckerTest {

	public static Stream<Arguments> invalidMarketStatusCheckerRules() {
		return Stream.of(
			Arguments.of((Object)null),
			Arguments.of((Object)new MarketStatusCheckerRule[0])
		);
	}

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

	@DisplayName("MarketStatusChecker 생성시 빈 배열이나 null을 전달하면 객체 생성시 인스턴스가 발생한다")
	@ParameterizedTest
	@MethodSource(value = "invalidMarketStatusCheckerRules")
	void shouldReturnFalse_whenEmptyCheckerRule(MarketStatusCheckerRule[] rules) {
		// given
		// when
		Throwable throwable = Assertions.catchThrowable(() -> new KoreanMarketStatusChecker(rules));
		// then
		Assertions.assertThat(throwable)
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("At least one rule must be provided");
	}
}
