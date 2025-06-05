package co.fineants.api.domain.holding.service;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import co.fineants.api.domain.holding.service.market_status_checker.MarketStatusCheckerRule;
import co.fineants.api.domain.holding.service.market_status_checker.TimeMarketStatusCheckerRule;
import co.fineants.api.domain.holding.service.market_status_checker.time_range.MarketTimeRange;
import co.fineants.api.domain.holding.service.market_status_checker.time_range.TimeRange;

class TimeMarketStatusCheckerRuleTest {

	private MarketStatusCheckerRule checker;

	public static Stream<Arguments> dateTimeSource() {
		return Stream.of(
			Arguments.of(LocalDateTime.of(2023, 10, 1, 9, 0)),   // 정규장 시작 시간
			Arguments.of(LocalDateTime.of(2023, 10, 1, 12, 0)),  // 정규장 중간 시간
			Arguments.of(LocalDateTime.of(2023, 10, 1, 15, 30)), // 정규장 종료 시간
			Arguments.of(LocalDateTime.of(2023, 10, 2, 9, 0))    // 다음 날 정규장 시작 시간
		);
	}

	public static Stream<Arguments> closeDateTimeSource() {
		return Stream.of(
			Arguments.of(LocalDateTime.of(2023, 10, 1, 8, 59)),   // 정규장 시작 전
			Arguments.of(LocalDateTime.of(2023, 10, 1, 15, 31)),  // 정규장 종료 후
			Arguments.of(LocalDateTime.of(2023, 10, 2, 8, 0)),    // 다음 날 정규장 시작 전
			Arguments.of(LocalDateTime.of(2023, 10, 2, 16, 0))    // 다음 날 정규장 종료 후
		);
	}

	@BeforeEach
	void setUp() {
		TimeRange timeRange = new MarketTimeRange();
		checker = new TimeMarketStatusCheckerRule(timeRange);
	}

	@DisplayName("정규장 중이면 true를 반환한다")
	@ParameterizedTest
	@MethodSource(value = "dateTimeSource")
	void isOpenDuringRegularHours(LocalDateTime dateTime) {
		// given

		// when
		boolean isOpen = checker.isOpen(dateTime);
		// then
		Assertions.assertThat(isOpen).isTrue();
	}

	@DisplayName("정규장이 아니면 false 반환한다")
	@ParameterizedTest
	@MethodSource(value = "closeDateTimeSource")
	void isNotOpenOutsideRegularHours(LocalDateTime dateTime) {
		// given

		// when
		boolean isClose = checker.isClose(dateTime);
		// then
		Assertions.assertThat(isClose).isTrue();
	}
}
