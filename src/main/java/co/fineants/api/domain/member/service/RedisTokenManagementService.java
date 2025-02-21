package co.fineants.api.domain.member.service;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class RedisTokenManagementService {

	private static final String LOGOUT = "logout";
	private static final Duration REFRESH_TOKEN_TIMEOUT = Duration.ofDays(7);
	private static final Duration ACCESS_TOKEN_TIMEOUT = Duration.ofMinutes(5);

	private final RedisTemplate<String, String> redisTemplate;

	public Optional<String> get(String key) {
		return Optional.ofNullable(redisTemplate.opsForValue().get(key));
	}

	public void banRefreshToken(String token) {
		banToken(token, REFRESH_TOKEN_TIMEOUT);
	}

	public void banAccessToken(String token) {
		banToken(token, ACCESS_TOKEN_TIMEOUT);
	}

	public void banToken(String token, Duration timeout) {
		redisTemplate.opsForValue().set(token, LOGOUT, timeout);
	}

	public boolean isAlreadyLogout(String token) {
		String logout = redisTemplate.opsForValue().get(token);
		return LOGOUT.equals(logout);
	}

	public void saveEmailVerifCode(String email, String verifCode) {
		long expirationTimeInMinutes = 5; // 5 minutes
		redisTemplate.opsForValue().set(email, verifCode, expirationTimeInMinutes, TimeUnit.MINUTES);
	}

	public void clear() {
		Set<String> keys = redisTemplate.keys("*");
		if (keys == null) {
			return;
		}
		for (String key : keys) {
			redisTemplate.delete(key);
		}
	}
}
