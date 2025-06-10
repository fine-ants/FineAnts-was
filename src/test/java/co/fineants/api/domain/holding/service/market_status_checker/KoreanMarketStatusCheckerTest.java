package co.fineants.api.domain.holding.service.market_status_checker;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import co.fineants.api.domain.holding.service.market_status_checker.time_range.KoreanMarketTimeRange;
import co.fineants.api.domain.holding.service.market_status_checker.time_range.TimeRange;

class KoreanMarketStatusCheckerTest {

	private MarketStatusCheckerRule rule;

	public static Stream<Arguments> invalidMarketStatusCheckerRules() {
		return Stream.of(
			Arguments.of((Object)null),
			Arguments.of((Object)new MarketStatusCheckerRule[0])
		);
	}

	public static Stream<Arguments> regularDateTimeSource() {
		return Stream.of(
			Arguments.of(LocalDateTime.of(2025, 6, 10, 9, 0), "정규시간 시작"),
			Arguments.of(LocalDateTime.of(2025, 6, 10, 15, 30), "정규시간 종료"),
			Arguments.of(LocalDateTime.of(2025, 6, 10, 12, 0), "정규시간 중간"),
			Arguments.of(LocalDateTime.of(2025, 6, 10, 10, 30), "정규시간 중간")
		);
	}

	public static Stream<Arguments> notRegularDateTimeSource() {
		return Stream.of(
			Arguments.of(LocalDateTime.of(2025, 6, 10, 8, 59), "정규시간 이전"),
			Arguments.of(LocalDateTime.of(2025, 6, 10, 15, 31), "정규시간 이후"),
			Arguments.of(LocalDateTime.of(2025, 6, 10, 0, 0), "정규시간 이전 (자정)"),
			Arguments.of(LocalDateTime.of(2025, 6, 10, 23, 59), "정규시간 이후 (23시59분)")
		);
	}

	@BeforeEach
	void setUp() {
		TimeRange regularTimeRange = new KoreanMarketTimeRange();
		rule = new TimeMarketStatusCheckerRule(regularTimeRange);
	}

	@DisplayName("정규시간 내에서는 true를 반환한다.")
	@ParameterizedTest(name = "{index} : {0} ({1})")
	@MethodSource(value = "regularDateTimeSource")
	void isOpen_shouldReturnTrue_whenDateTimeIsInRegularTime(LocalDateTime dateTime, String ignoredDescription) {
		// given
		MarketStatusChecker checker = new KoreanMarketStatusChecker(rule);
		// when
		boolean isOpen = checker.isOpen(dateTime);
		// then
		Assertions.assertThat(isOpen).isTrue();
	}

	@DisplayName("정규시간 외에는 false를 반환한다.")
	@ParameterizedTest(name = "{index} : {0} ({1})")
	@MethodSource(value = "notRegularDateTimeSource")
	void isOpen_shouldReturnFalse_whenDateTimeIsNotInRegularTime(LocalDateTime dateTime, String ignoredDescription) {
		// given
		MarketStatusChecker checker = new KoreanMarketStatusChecker(rule);
		// when
		boolean isOpen = checker.isOpen(dateTime);
		// then
		Assertions.assertThat(isOpen).isFalse();
	}

	@DisplayName("MarketStatusChecker 생성시 빈 배열이나 null을 전달하면 객체 생성시 인스턴스가 발생한다")
	@ParameterizedTest
	@MethodSource(value = "invalidMarketStatusCheckerRules")
	void created_shouldReturnFalse_whenEmptyCheckerRule(MarketStatusCheckerRule[] rules) {
		// given
		// when
		Throwable throwable = Assertions.catchThrowable(() -> new KoreanMarketStatusChecker(rules));
		// then
		Assertions.assertThat(throwable)
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("At least one rule must be provided");
	}

	@DisplayName("정규시간 외에서는 true를 반환한다.")
	@ParameterizedTest(name = "{index} : {0} ({1})")
	@MethodSource(value = "notRegularDateTimeSource")
	void isClose_shouldReturnFalse_whenDateTimeIsNotInRegularTime(LocalDateTime dateTime, String ignoredDescription) {
		// given
		MarketStatusChecker checker = new KoreanMarketStatusChecker(rule);
		// when
		boolean isClose = checker.isClose(dateTime);
		// then
		Assertions.assertThat(isClose).isTrue();
	}

	@DisplayName("정규시간 내에서는 false를 반환한다.")
	@ParameterizedTest(name = "{index} : {0} ({1})")
	@MethodSource(value = "regularDateTimeSource")
	void isClose_shouldReturnFalse_whenDateTimeIsInRegularTime(LocalDateTime dateTime, String ignoredDescription) {
		// given
		MarketStatusChecker checker = new KoreanMarketStatusChecker(rule);
		// when
		boolean isClose = checker.isClose(dateTime);
		// then
		Assertions.assertThat(isClose).isFalse();
	}

	@DisplayName("평일 정규시간 내에서는 true를 반환한다")
	@ParameterizedTest
	@MethodSource(value = "regularDateTimeSource")
	void isOpen_shouldReturnTrue_whenWeekDayAndDateTimeIsInRegularTime(LocalDateTime dateTime,
		String ignoredDescription) {
		// given
		MarketStatusCheckerRule weekdayRule = new WeekdayMarketStatusCheckerRule();
		MarketStatusChecker checker = new KoreanMarketStatusChecker(rule, weekdayRule);
		// when
		boolean isOpen = checker.isOpen(dateTime);
		// then
		Assertions.assertThat(isOpen).isTrue();
	}
}
