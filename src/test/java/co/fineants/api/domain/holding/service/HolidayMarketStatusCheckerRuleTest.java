package co.fineants.api.domain.holding.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.holding.service.market_status_checker.HolidayMarketStatusCheckerRule;
import co.fineants.api.domain.holding.service.market_status_checker.MarketStatusCheckerRule;
import co.fineants.api.domain.holiday.domain.entity.Holiday;
import co.fineants.api.domain.holiday.repository.HolidayRepository;

class HolidayMarketStatusCheckerRuleTest extends AbstractContainerBaseTest {

	private MarketStatusCheckerRule checker;

	@Autowired
	private HolidayRepository holidayRepository;

	@BeforeEach
	void setUp() {
		checker = new HolidayMarketStatusCheckerRule(holidayRepository);
	}

	@DisplayName("휴장일이 아니면 false를 반환한다")
	@Test
	void givenDateTime_whenNotHoliday_thenReturnTrue() {
		// given
		holidayRepository.save(Holiday.close(LocalDate.of(2025, 6, 6)));
		LocalDateTime dateTime = LocalDateTime.of(2025, 6, 6, 9, 0);
		// when
		boolean isOpen = checker.isOpen(dateTime);
		// then
		Assertions.assertThat(isOpen).isFalse();
	}
}
