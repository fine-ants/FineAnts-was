package co.fineants.api.domain.holding.service.market_status_checker;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.holiday.domain.entity.Holiday;
import co.fineants.api.domain.holiday.repository.HolidayRepository;

class HolidayMarketStatusCheckerRuleTest extends AbstractContainerBaseTest {

	@Autowired
	private HolidayRepository repository;
	private LocalDateTime dateTime;
	@Autowired
	@Qualifier("holidayMarketStatusCheckerRule")
	private MarketStatusCheckerRule rule;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@BeforeEach
	void setUp() {
		dateTime = LocalDateTime.of(2025, 6, 6, 9, 0);
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

	@DisplayName("dateTime에 따른 공휴일 캐시가 존재하는 경우 캐시값을 반환한다")
	@Test
	void isOpen_shouldReturnFalseAndContainsCacheInRedis_whenContainsCacheByDateTime() {
		// given
		LocalDate date = dateTime.toLocalDate();
		Holiday holiday = Holiday.close(date);
		repository.save(holiday);
		// when
		boolean isOpen = rule.isOpen(dateTime);
		// then
		assertThat(isOpen).isFalse();
		boolean cachedIsOpen = Boolean.parseBoolean(
			Objects.requireNonNull(redisTemplate.opsForValue().get("holidayCache::" + date)).toString());
		assertThat(cachedIsOpen).isFalse();
	}
}
