package co.fineants.api.domain.member.service;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerifyCodeRedisRepository implements VerifyCodeRepository {
	private final RedisTemplate<String, String> redisTemplate;

	@Override
	public Optional<String> get(String email) {
		return Optional.ofNullable(redisTemplate.opsForValue().get(email));
	}

	@Override
	public void save(String email, String verifyCode) {
		Duration timeout = Duration.ofMinutes(5);
		redisTemplate.opsForValue().set(email, verifyCode, timeout);
	}
}
