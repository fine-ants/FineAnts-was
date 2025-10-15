package co.fineants.api.domain.member.service;

import java.time.Duration;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class RedisJwtRepository implements JwtRepository {

	private static final String LOGOUT = "logout";
	private static final Duration REFRESH_TOKEN_TIMEOUT = Duration.ofDays(7);
	private static final Duration ACCESS_TOKEN_TIMEOUT = Duration.ofMinutes(5);

	private final RedisTemplate<String, String> redisTemplate;

	@Override
	public void banRefreshToken(String token) {
		banToken(token, REFRESH_TOKEN_TIMEOUT);
	}

	@Override
	public void banAccessToken(String token) {
		banToken(token, ACCESS_TOKEN_TIMEOUT);
	}

	@Override
	public boolean isAlreadyLogout(String token) {
		String logout = redisTemplate.opsForValue().get(token);
		return LOGOUT.equals(logout);
	}

	@Override
	public void clear() {
		Set<String> keys = redisTemplate.keys("*");
		if (keys == null) {
			return;
		}
		for (String key : keys) {
			if (LOGOUT.equals(redisTemplate.opsForValue().get(key))) {
				redisTemplate.delete(key);
			}
		}
	}

	private void banToken(String token, Duration timeout) {
		redisTemplate.opsForValue().set(token, LOGOUT, timeout);
	}
}
