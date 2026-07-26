package co.fineants.api.domain.kis.repository.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

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

	@DisplayName("액세스 토큰 저장")
	@Test
	void should_save_access_token() {
		// given
		LocalDateTime baseTime = LocalDate.of(2026, 12, 23).atStartOfDay();
		KisAccessToken kisAccessToken = TestDataFactory.createKisAccessToken(baseTime);
		// when & then
		Assertions.assertThatCode(() -> repository.save(kisAccessToken))
			.doesNotThrowAnyException();
	}

	@DisplayName("액세스 토큰 저장 - 액세스 토큰이 null이어도 예외가 발생하지 않는다")
	@Test
	void should_does_not_throw_exception_when_access_token_is_null() {
		// given
		KisAccessToken kisAccessToken = null;
		// when & then
		Assertions.assertThatCode(() -> repository.save(kisAccessToken))
			.doesNotThrowAnyException();
	}

	@DisplayName("액세스 토큰 조회")
	@Test
	void should_return_access_token() {
		// given
		LocalDateTime baseTime = LocalDate.of(2026, 12, 23).atStartOfDay();
		KisAccessToken kisAccessToken = TestDataFactory.createKisAccessToken(baseTime);
		repository.save(kisAccessToken);
		// when
		Optional<KisAccessToken> actual = repository.get();
		// then
		Assertions.assertThat(actual).isPresent();
	}

	@DisplayName("액세스 토큰 조회 - 액세스 토큰이 저장되어 있지 않으면 Empty Optional을 반환해야 한다")
	@Test
	void should_return_empty_optional_when_not_saved_access_token() {
		// when
		Optional<KisAccessToken> actual = repository.get();
		// then
		Assertions.assertThat(actual).isEmpty();
	}

	@DisplayName("액세스 토큰 만료 여부 - 시간이 만료시간보다 크면 true를 반환해야 한다")
	@Test
	void should_return_true_when_date_time_is_greater_than_base_time() {
		// given
		LocalDateTime baseTime = LocalDate.of(2023, 12, 23).atStartOfDay();
		KisAccessToken accessToken = TestDataFactory.createKisAccessToken(baseTime);
		repository.save(accessToken);
		LocalDateTime expiredDateTime = baseTime.plusHours(24).plusSeconds(1);
		// when
		boolean actual = repository.isAccessTokenExpired(expiredDateTime);
		// then
		Assertions.assertThat(actual).isTrue();
	}

	@DisplayName("액세스 토큰 만료 여부 - 액세스 토큰이 저장되어 있지 않으면 true를 반환한다")
	@Test
	void should_return_true_when_not_saved_access_token() {
		// given
		LocalDateTime baseTime = LocalDate.of(2023, 12, 23).atStartOfDay();
		LocalDateTime expiredDateTime = baseTime.plusHours(24);
		// when
		boolean actual = repository.isAccessTokenExpired(expiredDateTime);
		// then
		Assertions.assertThat(actual).isTrue();
	}

	@DisplayName("액세스 토큰 만료 여부 - 시간이 만료시간보다 같거나 작으면 false를 반환해야 한다")
	@Test
	void should_return_false_when_date_time_is_equal_less_than_base_time() {
		// given
		LocalDateTime baseTime = LocalDate.of(2023, 12, 23).atStartOfDay();
		KisAccessToken accessToken = TestDataFactory.createKisAccessToken(baseTime);
		repository.save(accessToken);
		// when & then
		Assertions.assertThat(repository.isAccessTokenExpired(baseTime.plusHours(24))).isFalse();
		Assertions.assertThat(repository.isAccessTokenExpired(baseTime.plusHours(24).minusSeconds(1))).isFalse();
	}

	@DisplayName("인증 헤더 문자열 생성")
	@Test
	void should_return_authorization() {
		// given
		LocalDateTime baseTime = LocalDate.of(2023, 12, 23).atStartOfDay();
		KisAccessToken accessToken = TestDataFactory.createKisAccessToken(baseTime);
		repository.save(accessToken);
		// when
		String authorization = repository.createAuthorization();
		// then
		Assertions.assertThat(authorization).isEqualTo("Bearer accessToken");
	}
}
