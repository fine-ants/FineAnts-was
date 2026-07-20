package co.fineants.api.domain.holding.service.market_status_checker;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.fineants.api.domain.holiday.domain.entity.Holiday;
import co.fineants.api.domain.holiday.repository.HolidayRepository;

@ExtendWith(MockitoExtension.class)
class HolidayMarketStatusCheckerRuleUnitTest {
	@Mock
	private HolidayRepository repository;
	private LocalDateTime dateTime;
	private MarketStatusCheckerRule rule;

	@BeforeEach
	void setUp() {
		dateTime = LocalDateTime.of(2025, 6, 6, 9, 0);
		rule = new HolidayMarketStatusCheckerRule(repository);
	}

	@DisplayName("dateTime이 공휴일인 경우에는 false를 반환한다")
	@Test
	void should_return_false_when_date_time_is_holiday() {
		// given
		BDDMockito.given(repository.findByBaseDate(dateTime.toLocalDate()))
			.willReturn(Optional.of(Holiday.close(dateTime.toLocalDate())));
		// when
		boolean isOpen = rule.isOpen(dateTime);
		// then
		assertThat(isOpen).isFalse();
	}

	@DisplayName("dateTime이 공휴일이 아닌 경우에는 true를 반환한다")
	@Test
	void should_return_true_when_date_time_is_not_holiday() {
		// given
		BDDMockito.given(repository.findByBaseDate(dateTime.toLocalDate()))
			.willReturn(Optional.empty());
		// when
		boolean isOpen = rule.isOpen(dateTime);
		// then
		assertThat(isOpen).isTrue();
	}
}
