package co.fineants.api.domain.holding.service.market_status_checker;

import java.time.LocalDateTime;

import org.springframework.cache.annotation.Cacheable;

import co.fineants.api.domain.holiday.repository.HolidayRepository;

public class HolidayMarketStatusCheckerRule implements MarketStatusCheckerRule {
	private final HolidayRepository repository;

	public HolidayMarketStatusCheckerRule(HolidayRepository repository) {
		this.repository = repository;
	}

	/**
	 * 데이터베이스에 Holiday 엔티티가 없으면 휴장일이 아니므로 true를 반환한다
	 * Holiday 테이블의 isOpen 컬럼값은 무조건 false로 저장되어 있다
	 */
	@Override
	@Cacheable(value = "holidayCache", key = "#dateTime.toLocalDate().toString()")
	public boolean isOpen(LocalDateTime dateTime) {
		return repository.findByBaseDate(dateTime.toLocalDate())
			.isEmpty();
	}
}
