package co.fineants.api.domain.holding.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import co.fineants.api.domain.holding.service.market_status_checker.MarketStatusCheckerRule;
import co.fineants.api.domain.holding.service.market_status_checker.WeekdayMarketStatusCheckerRule;

class WeekdayMarketStatusCheckerRuleTest {

	private MarketStatusCheckerRule checker;

	public static Stream<Arguments> weekdayDateTimeSource() {
		return Stream.of(
			Arguments.of(LocalDateTime.of(2023, 10, 2, 9, 0)),   // 월요일
			Arguments.of(LocalDateTime.of(2023, 10, 3, 12, 0)),  // 화요일
			Arguments.of(LocalDateTime.of(2023, 10, 4, 15, 30)), // 수요일
			Arguments.of(LocalDateTime.of(2023, 10, 5, 8, 0)),   // 목요일
			Arguments.of(LocalDateTime.of(2023, 10, 6, 16, 0))    // 금요일
		);
	}

	public static Stream<Arguments> weekendDateTimeSource() {
		return Stream.of(
			Arguments.of(LocalDateTime.of(2023, 10, 7, 9, 0)),   // 토요일
			Arguments.of(LocalDateTime.of(2023, 10, 8, 12, 0))  // 일요일
		);
	}

	@BeforeEach
	void setUp() {
		checker = new WeekdayMarketStatusCheckerRule();
	}

	@DisplayName("주말이 아닌 평일이면 true를 반환한다")
	@ParameterizedTest
	@MethodSource(value = "weekdayDateTimeSource")
	void givenDateTime_whenDateTimeIsWeekday_thenReturnTrue(LocalDateTime dateTime) {
		// given

		// when
		boolean isOpen = checker.isOpen(dateTime);
		// then
		assertThat(isOpen).isTrue();
	}

	@DisplayName("주말이면 false를 반환한다")
	@ParameterizedTest
	@MethodSource(value = "weekendDateTimeSource")
	void givenDateTime_whenDateTimeIsWeekend_thenReturnTrue(LocalDateTime dateTime) {
		// given

		// when
		boolean isClose = checker.isClose(dateTime);
		// then
		assertThat(isClose).isTrue();
	}
}
