package co.fineants.member.infrastructure;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import co.fineants.member.domain.VerifyCodeRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class VerifyCodeRedisRepository implements VerifyCodeRepository {
	private static final Duration TIMEOUT = Duration.ofMinutes(5);
	private final RedisTemplate<String, String> redisTemplate;

	@Override
	public Optional<String> get(String email) {
		return Optional.ofNullable(redisTemplate.opsForValue().get(email));
	}

	@Override
	public void save(String email, String verifyCode) {
		redisTemplate.opsForValue().set(email, verifyCode, TIMEOUT);
	}
}
