package co.fineants.api.domain.kis.repository.infrastructure;

import static co.fineants.api.domain.kis.service.KisAccessTokenService.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import co.fineants.api.domain.kis.client.KisAccessToken;
import co.fineants.api.domain.kis.domain.repository.KisAccessTokenRepository;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.global.util.ObjectMapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
@Primary
public class KisAccessTokenRedisRepository implements KisAccessTokenRepository {

	private final RedisTemplate<String, Object> redisTemplate;
	private final LocalDateTimeService timeService;

	@Override
	public void save(KisAccessToken accessToken) {
		save(accessToken, timeService.getLocalDateTimeWithNow());
	}

	@Override
	public void save(KisAccessToken accessToken, LocalDateTime expiredDateTime) {
		try {
			redisTemplate.opsForValue().set(ACCESS_TOKEN_MAP_KEY,
				ObjectMapperUtil.serialize(accessToken),
				accessToken.betweenSecondFrom(expiredDateTime));
		} catch (RedisSystemException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public Optional<KisAccessToken> get() {
		Object result = redisTemplate.opsForValue().get(ACCESS_TOKEN_MAP_KEY);
		if (result == null) {
			return Optional.empty();
		}
		return Optional.of(ObjectMapperUtil.deserialize((String)result, KisAccessToken.class));
	}

	@Override
	public boolean isAccessTokenExpired(LocalDateTime dateTime) {
		// todo: service로 이전할 예정
		throw new UnsupportedOperationException();
	}

	@Override
	public String createAuthorization() {
		// todo: service로 이전할 예정
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isTokenExpiringSoon(LocalDateTime localDateTime) {
		// todo: service로 이전할 예정
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		Boolean isDeleted = redisTemplate.delete(ACCESS_TOKEN_MAP_KEY);
		log.info("액세스 토큰 제거 완료 여부 : {}", isDeleted);
	}
}
