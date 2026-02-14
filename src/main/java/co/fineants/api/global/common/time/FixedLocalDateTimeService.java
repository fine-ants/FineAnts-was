package co.fineants.api.global.common.time;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class FixedLocalDateTimeService implements LocalDateTimeService {

	private final LocalDateTime fixedDateTime;

	public FixedLocalDateTimeService() {
		this.fixedDateTime = LocalDate.of(2026, 2, 13).atTime(17, 0, 0);
	}

	@Override
	public LocalDate getLocalDateWithNow() {
		return fixedDateTime.toLocalDate();
	}

	@Override
	public LocalDateTime getLocalDateTimeWithNow() {
		return fixedDateTime;
	}
}
