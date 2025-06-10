package co.fineants.api.domain.holding.service.market_status_checker;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.holiday.domain.entity.Holiday;
import co.fineants.api.domain.holiday.repository.HolidayRepository;

class HolidayMarketStatusCheckerRuleTest extends AbstractContainerBaseTest {

	@Autowired
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
	void isOpen_shouldReturnFalse_whenDateTimeIsHoliday() {
		// given
		Holiday holiday = Holiday.close(dateTime.toLocalDate());
		repository.save(holiday);
		// when
		boolean isOpen = rule.isOpen(dateTime);
		// then
		assertThat(isOpen).isFalse();
	}

	@DisplayName("dateTime이 공휴일이 아닌 경우에는 true를 반환한다")
	@Test
	void isOpen_shouldReturnTrue_whenDateTimeIsNotHoliday() {
		// given
		// when
		boolean isOpen = rule.isOpen(dateTime);
		// then
		assertThat(isOpen).isTrue();
	}
}
