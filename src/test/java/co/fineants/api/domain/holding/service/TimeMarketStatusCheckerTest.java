package co.fineants.api.domain.holding.service;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TimeMarketStatusCheckerTest {

	private MarketStatusChecker checker;

	public static Stream<Arguments> dateTimeSource() {
		return Stream.of(
			Arguments.of(LocalDateTime.of(2023, 10, 1, 9, 0)),   // 정규장 시작 시간
			Arguments.of(LocalDateTime.of(2023, 10, 1, 12, 0)),  // 정규장 중간 시간
			Arguments.of(LocalDateTime.of(2023, 10, 1, 15, 30)), // 정규장 종료 시간
			Arguments.of(LocalDateTime.of(2023, 10, 2, 9, 0))    // 다음 날 정규장 시작 시간
		);
	}

	@BeforeEach
	void setUp() {
		checker = new TimeMarketStatusChecker();
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
}
