package co.fineants.api.domain.kis.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.kis.client.KisAccessToken;
import co.fineants.api.domain.kis.domain.repository.KisAccessTokenRepository;

@ExtendWith(MockitoExtension.class)
class KisAccessTokenServiceUnitTest {

	@InjectMocks
	private KisAccessTokenService service;

	@Mock
	private KisAccessTokenRepository repository;

	@DisplayName("액세스 토큰 저장 - kis 액세스 토큰맵을 저장한다")
	@Test
	void should_set_access_token_map() {
		// given
		LocalDateTime createdTime = LocalDate.of(2026, 7, 26).atStartOfDay();
		KisAccessToken kisAccessToken = TestDataFactory.createKisAccessToken(createdTime);

		LocalDateTime now = createdTime.minusHours(24);

		// when & then
		Assertions.assertThatCode(() -> service.setAccessTokenMap(kisAccessToken, now))
			.doesNotThrowAnyException();
	}

	@DisplayName("이미 만료된 액세스 토큰을 저장할 수 없다.")
	@Test
	void should_not_set_access_token_map_when_expired_access_token() {
		// given
		LocalDateTime createdAt = LocalDate.of(2026, 7, 26).atStartOfDay();
		KisAccessToken accessToken = TestDataFactory.createKisAccessToken(createdAt);

		LocalDateTime now = createdAt.plusSeconds(accessToken.getExpiresIn())
			.plusSeconds(1);

		// when & then
		Assertions.assertThatCode(() -> service.setAccessTokenMap(accessToken, now))
			.doesNotThrowAnyException();
	}

	@DisplayName("액세스 토큰 조회 - Redis에 저장된 액세스 토큰 맵을 조회한다")
	@Test
	void should_return_access_token() {
		// given
		LocalDateTime createdAt = LocalDate.of(2026, 7, 26).atStartOfDay();
		KisAccessToken accessToken = TestDataFactory.createKisAccessToken(createdAt);

		BDDMockito.given(repository.get())
			.willReturn(Optional.of(accessToken));
		// when
		Optional<KisAccessToken> actual = service.getAccessTokenMap();

		// then
		int expiredSeconds = 86400;
		KisAccessToken expected = KisAccessToken.bearerType("accessToken", createdAt.plusSeconds(expiredSeconds),
			expiredSeconds);
		Assertions.assertThat(actual).contains(expected);
	}

	@DisplayName("저장소에 accessToken이 없는 경우 Optional.empty()를 반환한다")
	@Test
	void should_return_empty_optional_when_get_access_token() {
		// given
		BDDMockito.given(repository.get())
			.willReturn(Optional.empty());
		// when
		Optional<KisAccessToken> optionalKisAccessToken = service.getAccessTokenMap();
		// then
		Assertions.assertThat(optionalKisAccessToken).isEmpty();
	}
}
