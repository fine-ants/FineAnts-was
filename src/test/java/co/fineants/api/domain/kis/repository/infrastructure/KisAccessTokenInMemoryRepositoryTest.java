package co.fineants.api.domain.kis.repository.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.kis.client.KisAccessToken;
import co.fineants.api.domain.kis.domain.repository.KisAccessTokenRepository;

class KisAccessTokenInMemoryRepositoryTest {

	private KisAccessTokenRepository repository;

	@BeforeEach
	void setUp() {
		repository = new KisAccessTokenInMemoryRepository();
	}

	@DisplayName("액세스 토큰이 만료되었다")
	@Test
	void should_return_true_when_date_time_is_greater_than_base_time() {
		// given
		LocalDateTime baseTime = LocalDate.of(2023, 12, 23).atStartOfDay();
		KisAccessToken accessToken = TestDataFactory.createKisAccessToken(baseTime);
		repository.refreshAccessToken(accessToken);
		LocalDateTime expiredDateTime = baseTime.plusHours(24).plusSeconds(1);
		// when
		boolean actual = repository.isAccessTokenExpired(expiredDateTime);
		// then
		Assertions.assertThat(actual).isTrue();
	}
}
