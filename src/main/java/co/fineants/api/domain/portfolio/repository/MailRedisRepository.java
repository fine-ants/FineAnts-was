package co.fineants.api.domain.portfolio.repository;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MailRedisRepository {
	private final RedisTemplate<String, String> redisTemplate;

	public boolean hasMailSentHistoryForTargetGain(Portfolio portfolio) {
		String value = redisTemplate.opsForValue().get(createMailSentHistoryKeyForTargetGain(portfolio));
		return value != null;
	}

	private String createMailSentHistoryKeyForTargetGain(Portfolio portfolio) {
		return String.format("targetGainMail:%d", portfolio.getId());
	}

	public boolean hasMailSentHistoryForMaximumLoss(Portfolio portfolio) {
		String value = redisTemplate.opsForValue().get(createMailSentHistoryKeyForMaximumLoss(portfolio));
		return value != null;
	}

	private String createMailSentHistoryKeyForMaximumLoss(Portfolio portfolio) {
		return String.format("MaximumLossMail:%d", portfolio.getId());
	}

	public void setMailSentHistoryForTargetGain(Portfolio portfolio) {
		redisTemplate.opsForValue().set(
			createMailSentHistoryKeyForTargetGain(portfolio),
			Boolean.TRUE.toString(),
			Duration.ofDays(1));
	}

	public void setMailSentHistoryMaximumLoss(Portfolio portfolio) {
		redisTemplate.opsForValue().set(
			createMailSentHistoryKeyForMaximumLoss(portfolio),
			Boolean.TRUE.toString(),
			Duration.ofDays(1));
	}
}
