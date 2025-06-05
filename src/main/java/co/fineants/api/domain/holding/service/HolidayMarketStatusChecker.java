package co.fineants.api.domain.holding.service;

import java.time.LocalDateTime;

import co.fineants.api.domain.holiday.domain.entity.Holiday;
import co.fineants.api.domain.holiday.repository.HolidayRepository;

public class HolidayMarketStatusChecker implements MarketStatusChecker {
	private final HolidayRepository repository;

	public HolidayMarketStatusChecker(HolidayRepository repository) {
		this.repository = repository;
	}

	/**
	 * 데이터베이스에 Holiday 엔티티가 없으면 휴장일이 아니므로 true를 반환한다
	 * Holiday 테이블의 isOpen 컬럼값은 무조건 false로 저장되어 있다
	 */
	@Override
	public boolean isOpen(LocalDateTime dateTime) {
		return repository.findByBaseDate(dateTime.toLocalDate())
			.map(Holiday::isOpenMarket)
			.orElse(Boolean.TRUE);
	}
}
