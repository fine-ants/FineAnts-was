package co.fineants.api.domain.holding.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.holding.service.market_status_checker.HolidayMarketStatusCheckerRule;
import co.fineants.api.domain.holding.service.market_status_checker.KoreanMarketStatusChecker;
import co.fineants.api.domain.holding.service.market_status_checker.TimeMarketStatusCheckerRule;
import co.fineants.api.domain.holding.service.market_status_checker.WeekdayMarketStatusCheckerRule;
import co.fineants.api.domain.holding.service.market_status_checker.time_range.KoreanMarketTimeRange;
import co.fineants.api.domain.holding.service.market_status_checker.time_range.TimeRange;
import co.fineants.api.domain.holiday.repository.HolidayRepository;

@Configuration
public class MarketStatusCheckerConfig {
	@Bean
	public TimeMarketStatusCheckerRule timeMarketStatusCheckerRule() {
		TimeRange timeRange = new KoreanMarketTimeRange();
		return new TimeMarketStatusCheckerRule(timeRange);
	}

	@Bean
	public WeekdayMarketStatusCheckerRule weekdayMarketStatusCheckerRule() {
		return new WeekdayMarketStatusCheckerRule();
	}

	@Bean
	public HolidayMarketStatusCheckerRule holidayMarketStatusCheckerRule(HolidayRepository repository) {
		return new HolidayMarketStatusCheckerRule(repository);
	}

	@Bean
	public KoreanMarketStatusChecker koreanMarketStatusChecker(
		TimeMarketStatusCheckerRule timeMarketStatusCheckerRule,
		WeekdayMarketStatusCheckerRule weekdayMarketStatusCheckerRule,
		HolidayMarketStatusCheckerRule holidayMarketStatusCheckerRule) {
		return new KoreanMarketStatusChecker(
			timeMarketStatusCheckerRule,
			weekdayMarketStatusCheckerRule,
			holidayMarketStatusCheckerRule
		);
	}
}
