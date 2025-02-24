package co.fineants.api.domain.notification.repository;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.stock_target_price.domain.entity.TargetPriceNotification;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class NotificationSentRepository {
	private static final String TARGET_GAIN_PREFIX = "targetGainSent";
	private static final String MAX_LOSS_PREFIX = "maxLossSent";
	private static final String TARGET_PRICE_PREFIX = "targetPriceSent";
	private static final String TARGET_GAIN_FORMAT = TARGET_GAIN_PREFIX + ":%d";
	private static final String MAX_LOSS_FORMAT = MAX_LOSS_PREFIX + ":%d";
	private static final String TARGET_PRICE_FORMAT = TARGET_PRICE_PREFIX + ":%d";

	private final RedisTemplate<String, String> redisTemplate;
	private static final Duration TIMEOUT = Duration.ofHours(24L);
	private static final String TRUE = "true";

	public void addTargetGainSendHistory(Notification notification) {
		redisTemplate.opsForValue()
			.set(notification.formatted(TARGET_GAIN_FORMAT), TRUE, TIMEOUT);
	}

	public void addMaxLossSendHistory(Notification notification) {
		redisTemplate.opsForValue()
			.set(notification.formatted(MAX_LOSS_FORMAT), TRUE, TIMEOUT);
	}

	public void addTargetPriceSendHistory(Notification notification) {
		redisTemplate.opsForValue()
			.set(notification.formatted(TARGET_PRICE_FORMAT), TRUE, TIMEOUT);
	}

	public boolean hasTargetGainSendHistory(Long portfolioId) {
		String result = redisTemplate.opsForValue().get(String.format(TARGET_GAIN_FORMAT, portfolioId));
		return result != null;
	}

	public boolean hasMaxLossSendHistory(Long portfolioId) {
		String result = redisTemplate.opsForValue().get(String.format(MAX_LOSS_FORMAT, portfolioId));
		return result != null;
	}

	public boolean hasTargetPriceSendHistory(Long targetPriceNotificationId) {
		String result = redisTemplate.opsForValue().get(String.format(TARGET_PRICE_FORMAT, targetPriceNotificationId));
		return result != null;
	}

	public boolean hasTargetPriceSendHistory(TargetPriceNotification targetPriceNotification) {
		Long id = targetPriceNotification.getId();
		String result = redisTemplate.opsForValue().get(String.format(TARGET_PRICE_FORMAT, id));
		return result != null;
	}

	public void clear() {
		String targetGainPattern = TARGET_GAIN_PREFIX + ":*";
		String maxLossPattern = MAX_LOSS_PREFIX + ":*";
		String targetPricePattern = TARGET_PRICE_PREFIX + ":*";
		List<String> patterns = List.of(targetGainPattern, maxLossPattern, targetPricePattern);

		for (String pattern : patterns) {
			Set<String> keys = redisTemplate.keys(pattern);
			clear(keys);
		}
	}

	private void clear(Set<String> keys) {
		if (keys != null) {
			for (String key : keys) {
				redisTemplate.delete(key);
			}
		}
	}
}
