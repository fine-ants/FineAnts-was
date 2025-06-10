package co.fineants.api.domain.holding.service.market_status_checker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import co.fineants.api.domain.holding.service.market_status_checker.time_range.KoreanMarketTimeRange;
import co.fineants.api.domain.holding.service.market_status_checker.time_range.TimeRange;
import co.fineants.api.domain.holiday.domain.entity.Holiday;
import co.fineants.api.domain.holiday.repository.HolidayRepository;

class KoreanMarketStatusCheckerTest {

	private MarketStatusCheckerRule rule;
	private MarketStatusCheckerRule weekdayRule;

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

	public static Stream<Arguments> weekendDateTimeSource() {
		return Stream.of(
			Arguments.of(LocalDateTime.of(2025, 6, 14, 8, 59), "토요일 정규시간 이전"),
			Arguments.of(LocalDateTime.of(2025, 6, 14, 15, 31), "토요일 정규시간 이후"),
			Arguments.of(LocalDateTime.of(2025, 6, 14, 0, 0), "토요일 정규시간 이전 (자정)"),
			Arguments.of(LocalDateTime.of(2025, 6, 14, 23, 59), "토요일 정규시간 이후 (23시59분)"),
			Arguments.of(LocalDateTime.of(2025, 6, 15, 8, 59), "일요일 정규시간 이전"),
			Arguments.of(LocalDateTime.of(2025, 6, 15, 15, 31), "일요일 정규시간 이후"),
			Arguments.of(LocalDateTime.of(2025, 6, 15, 0, 0), "일요일 정규시간 이전 (자정)"),
			Arguments.of(LocalDateTime.of(2025, 6, 15, 23, 59), "일요일 정규시간 이후 (23시59분)"),
			Arguments.of(LocalDateTime.of(2025, 6, 14, 9, 0), "토요일 정규시간 시작"),
			Arguments.of(LocalDateTime.of(2025, 6, 14, 15, 30), "토요일 정규시간 종료"),
			Arguments.of(LocalDateTime.of(2025, 6, 14, 12, 0), "토요일 정규시간 중간"),
			Arguments.of(LocalDateTime.of(2025, 6, 14, 10, 30), "토요일 정규시간 중간"),
			Arguments.of(LocalDateTime.of(2025, 6, 15, 9, 0), "일요일 정규시간 시작"),
			Arguments.of(LocalDateTime.of(2025, 6, 15, 15, 30), "일요일 정규시간 종료"),
			Arguments.of(LocalDateTime.of(2025, 6, 15, 12, 0), "일요일 정규시간 중간"),
			Arguments.of(LocalDateTime.of(2025, 6, 15, 10, 30), "일요일 정규시간 중간")
		);
	}

	@BeforeEach
	void setUp() {
		TimeRange regularTimeRange = new KoreanMarketTimeRange();
		rule = new TimeMarketStatusCheckerRule(regularTimeRange);
		weekdayRule = new WeekdayMarketStatusCheckerRule();
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

	@DisplayName("MarketStatusChecker 생성시 빈 배열이나 null을 전달하면 객체 생성시 예외가 발생한다")
	@ParameterizedTest
	@MethodSource(value = "invalidMarketStatusCheckerRules")
	void created_shouldThrowException_whenEmptyCheckerRule(MarketStatusCheckerRule[] rules) {
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
	void isClose_shouldReturnTrue_whenDateTimeIsNotInRegularTime(LocalDateTime dateTime, String ignoredDescription) {
		// given
		MarketStatusChecker checker = new KoreanMarketStatusChecker(rule);
		// when
		boolean isClose = checker.isClosed(dateTime);
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
		boolean isClose = checker.isClosed(dateTime);
		// then
		Assertions.assertThat(isClose).isFalse();
	}

	@DisplayName("평일 정규시간 내에서는 true를 반환한다")
	@ParameterizedTest
	@MethodSource(value = "regularDateTimeSource")
	void isOpen_shouldReturnTrue_whenWeekDayAndDateTimeIsInRegularTime(LocalDateTime dateTime,
		String ignoredDescription) {
		// given
		MarketStatusChecker checker = new KoreanMarketStatusChecker(rule, weekdayRule);
		// when
		boolean isOpen = checker.isOpen(dateTime);
		// then
		Assertions.assertThat(isOpen).isTrue();
	}

	@DisplayName("주말시간에서는 false를 반환한다")
	@ParameterizedTest
	@MethodSource(value = "weekendDateTimeSource")
	void isOpen_shouldReturnFalse_whenWeekendAndDateTimeIsInRegularTime(LocalDateTime dateTime,
		String ignoredDescription) {
		// given
		MarketStatusChecker checker = new KoreanMarketStatusChecker(rule, weekdayRule);
		// when
		boolean isOpen = checker.isOpen(dateTime);
		// then
		Assertions.assertThat(isOpen).isFalse();
	}

	@DisplayName("공휴일인 경우에는 false를 반환한다")
	@ParameterizedTest
	@MethodSource(value = "regularDateTimeSource")
	void isOpen_shouldReturnFalse_whenHoliday(LocalDateTime dateTime) {
		// given
		HolidayRepository holidayRepository = Mockito.mock(HolidayRepository.class);
		LocalDate holidayDate = dateTime.toLocalDate();
		Holiday holiday = Holiday.close(holidayDate);
		BDDMockito.given(holidayRepository.findByBaseDate(holidayDate))
			.willReturn(Optional.of(holiday));
		MarketStatusCheckerRule holidayRule = new HolidayMarketStatusCheckerRule(holidayRepository);
		MarketStatusChecker checker = new KoreanMarketStatusChecker(rule, weekdayRule, holidayRule);
		// when
		boolean isOpen = checker.isOpen(dateTime);
		// then
		Assertions.assertThat(isOpen).isFalse();
	}

	@DisplayName("공휴일인 경우에는 true를 반환한다")
	@ParameterizedTest
	@MethodSource(value = "regularDateTimeSource")
	void isClose_shouldReturnTrue_whenHoliday(LocalDateTime dateTime) {
		// given
		HolidayRepository holidayRepository = Mockito.mock(HolidayRepository.class);
		LocalDate holidayDate = dateTime.toLocalDate();
		Holiday holiday = Holiday.close(holidayDate);
		BDDMockito.given(holidayRepository.findByBaseDate(holidayDate))
			.willReturn(Optional.of(holiday));
		MarketStatusCheckerRule holidayRule = new HolidayMarketStatusCheckerRule(holidayRepository);
		MarketStatusChecker checker = new KoreanMarketStatusChecker(rule, weekdayRule, holidayRule);
		// when
		boolean isClose = checker.isClosed(dateTime);
		// then
		Assertions.assertThat(isClose).isTrue();
	}
}
