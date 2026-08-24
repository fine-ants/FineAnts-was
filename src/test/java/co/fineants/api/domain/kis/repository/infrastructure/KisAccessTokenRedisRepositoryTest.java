package co.fineants.api.domain.kis.repository.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.kis.client.KisAccessToken;

class KisAccessTokenRedisRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private KisAccessTokenRedisRepository repository;

	@DisplayName("액세스 토큰 저장")
	@Test
	void should_save_access_token() {
		// given
		LocalDateTime baseTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		KisAccessToken kisAccessToken = TestDataFactory.createKisAccessToken(baseTime);
		// when
		repository.save(kisAccessToken);
		// then
		Assertions.assertThat(repository.get()).contains(kisAccessToken);
	}

	@DisplayName("액세스 토큰 저장 - null값을 저장하려고 하면 예외가 발생해야 한다.")
	@Test
	void should_throw_exception_when_access_token_is_null() {
		// given
		KisAccessToken kisAccessToken = null;
		// when
		Throwable throwable = Assertions.catchThrowable(() -> repository.save(kisAccessToken));
		// then
		Assertions.assertThat(throwable)
			.isInstanceOf(InvalidDataAccessApiUsageException.class)
			.hasMessage("accessToken is null object");
	}

	@DisplayName("액세스 토큰 조회")
	@Test
	void should_return_access_token_when_get_access_token() {
		// given
		LocalDateTime baseTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		KisAccessToken kisAccessToken = TestDataFactory.createKisAccessToken(baseTime);
		repository.save(kisAccessToken);
		// when
		Optional<KisAccessToken> findAccessToken = repository.get();
		// then
		Assertions.assertThat(findAccessToken).contains(kisAccessToken);
	}

	@DisplayName("액세스 토큰 조회 - 액세스 토큰이 없으면 Empty Optional을 반환해야 한다")
	@Test
	void should_return_empty_optional_when_not_saved_access_token() {
		// when
		Optional<KisAccessToken> kisAccessToken = repository.get();
		// then
		Assertions.assertThat(kisAccessToken).isEmpty();
	}

	@DisplayName("엑세스 토큰 제거")
	@Test
	void should_delete_access_token_when_clear_access_token() {
		// given
		LocalDateTime baseTime = LocalDate.of(2026, 7, 27).atStartOfDay();
		KisAccessToken kisAccessToken = TestDataFactory.createKisAccessToken(baseTime);
		repository.save(kisAccessToken);
		// when
		repository.clear();
		// then
		Assertions.assertThat(repository.get()).isEmpty();
	}
}
