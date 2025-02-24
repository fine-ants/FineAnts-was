package co.fineants.api.domain.holiday.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.holiday.domain.entity.Holiday;
import co.fineants.api.domain.holiday.repository.HolidayRepository;
import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.domain.dto.response.KisHoliday;
import co.fineants.api.global.common.delay.DelayManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class HolidayService {

	private final KisClient kisClient;
	private final DelayManager delayManager;
	private final HolidayRepository repository;

	@Transactional
	@CacheEvict(value = "holidayCache", allEntries = true)
	public List<Holiday> updateHoliday(LocalDate baseDate) {
		// 한국투자증권에 baseDate를 기준으로 기준일자 이후의 국내 휴장일을 조회합니다.
		List<Holiday> holidays = kisClient.fetchHolidays(baseDate)
			.map(kisHolidays -> kisHolidays.stream()
				.map(KisHoliday::toEntity)
				.toList())
			.blockOptional(delayManager.timeout())
			.orElseGet(Collections::emptyList);

		// 국내 휴장일 조회된 데이터중에서 개장하지 않는 데이터를 필터링
		List<Holiday> closeHolidays = holidays.stream()
			.filter(Holiday::isCloseMarket)
			.toList();
		// 중복 데이터 삭제
		deleteHolidays(closeHolidays);
		// 데이터 저장
		return saveHolidays(closeHolidays);
	}

	@NotNull
	private List<Holiday> saveHolidays(List<Holiday> holidays) {
		return repository.saveAll(holidays).stream()
			.sorted()
			.toList();
	}

	private void deleteHolidays(List<Holiday> holidays) {
		List<LocalDate> baseDates = holidays.stream()
			.map(Holiday::getBaseDate)
			.toList();
		int deleted = repository.deleteAllByBaseDate(baseDates);
		log.info("delete count: {}", deleted);
	}

	/**
	 * 매개변수로 받은 localDate가 휴장 여부를 반환
	 * <p>
	 * 캐시 TTL : 1 days
	 * </p>
	 * @param localDate the local date
	 * @return true: 휴장, false: 영업
	 */
	@Transactional(readOnly = true)
	@Cacheable(value = "holidayCache", key = "#localDate")
	public boolean isHoliday(LocalDate localDate) {
		return repository.findByBaseDate(localDate).isPresent();
	}
}
