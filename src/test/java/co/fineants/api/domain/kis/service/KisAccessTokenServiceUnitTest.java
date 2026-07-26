package co.fineants.api.domain.kis.service;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.kis.client.KisAccessToken;
import co.fineants.api.domain.kis.repository.infrastructure.KisAccessTokenRedisRepository;
import co.fineants.api.global.util.ObjectMapperUtil;

@ExtendWith(MockitoExtension.class)
class KisAccessTokenServiceUnitTest {

	@InjectMocks
	private KisAccessTokenService service;

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private ValueOperations<String, Object> valueOperations;

	@BeforeEach
	void setUp() {
		BDDMockito.given(redisTemplate.opsForValue())
			.willReturn(valueOperations);
	}

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

	@DisplayName("이미 만료된 액세스 토큰을 redis에 저장할 수 없다.")
	@Test
	void should_not_set_access_token_map_when_expired_access_token() {
		// given
		LocalDateTime createdAt = LocalDate.of(2026, 7, 26).atStartOfDay();
		KisAccessToken accessToken = TestDataFactory.createKisAccessToken(createdAt);

		LocalDateTime now = createdAt.plusSeconds(accessToken.getExpiresIn());
		Duration timeout = Duration.ofSeconds(accessToken.betweenSecondFrom(now).toSeconds());
		BDDMockito.willThrow(RedisSystemException.class)
			.given(valueOperations)
			.set(KisAccessTokenRedisRepository.ACCESS_TOKEN_MAP_KEY, ObjectMapperUtil.serialize(accessToken), timeout);

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
		String json = ObjectMapperUtil.serialize(accessToken);
		BDDMockito.given(valueOperations.get(KisAccessTokenRedisRepository.ACCESS_TOKEN_MAP_KEY))
			.willReturn(json);
		// when
		Optional<KisAccessToken> actual = service.getAccessTokenMap();

		// then
		int expiredSeconds = 86400;
		KisAccessToken expected = KisAccessToken.bearerType("accessToken", createdAt.plusSeconds(expiredSeconds),
			expiredSeconds);
		Assertions.assertThat(actual).contains(expected);
	}

	@DisplayName("Redis에 accessToken이 없는 경우 Optional.empty()를 반환한다")
	@Test
	void should_return_empty_optional_when_get_access_token() {
		// given
		BDDMockito.given(valueOperations.get(KisAccessTokenRedisRepository.ACCESS_TOKEN_MAP_KEY))
			.willReturn(null);
		// when
		Optional<KisAccessToken> optionalKisAccessToken = service.getAccessTokenMap();
		// then
		assertThat(optionalKisAccessToken).isEmpty();
	}
}
