package co.fineants.api.domain.kis.repository.infrastructure;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import co.fineants.api.domain.kis.client.KisAccessToken;
import co.fineants.api.domain.kis.domain.repository.KisAccessTokenRepository;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.global.util.ObjectMapperUtil;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
@Primary
public class KisAccessTokenRedisRepository implements KisAccessTokenRepository {
	private final RedisTemplate<String, Object> redisTemplate;
	private final LocalDateTimeService timeService;
	private final String key;

	public KisAccessTokenRedisRepository(
		RedisTemplate<String, Object> redisTemplate,
		LocalDateTimeService timeService,
		@Value("${kis.access-token.key:kis:accessTokenMap}") String key) {
		this.redisTemplate = redisTemplate;
		this.timeService = timeService;
		this.key = key;
	}

	@Override
	public void save(KisAccessToken accessToken) {
		save(accessToken, timeService.getLocalDateTimeWithNow());
	}

	@Override
	public void save(KisAccessToken accessToken, LocalDateTime now) {
		if (accessToken == null) {
			throw new IllegalArgumentException("accessToken is null object");
		}
		try {
			redisTemplate.opsForValue().set(key,
				ObjectMapperUtil.serialize(accessToken),
				accessToken.betweenSecondFrom(now));
		} catch (RedisSystemException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public Optional<KisAccessToken> get() {
		Object result = redisTemplate.opsForValue().get(key);
		if (result == null) {
			return Optional.empty();
		}
		return Optional.of(ObjectMapperUtil.deserialize((String)result, KisAccessToken.class));
	}

	@Override
	public String createAuthorization() {
		// todo: service로 이전할 예정
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		Boolean isDeleted = redisTemplate.delete(key);
		log.info("액세스 토큰 제거 완료 여부 : {}", isDeleted);
	}
}
