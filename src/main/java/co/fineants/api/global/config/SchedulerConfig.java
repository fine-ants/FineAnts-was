package co.fineants.api.global.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;

@EnableSchedulerLock(defaultLockAtLeastFor = "40s", defaultLockAtMostFor = "50s")
@EnableScheduling
@ConditionalOnProperty(value = "scheduling.enabled", havingValue = "true", matchIfMissing = true)
@Configuration
public class SchedulerConfig {

	private final Environment env;

	public SchedulerConfig(Environment env) {
		this.env = env;
	}

	@Bean
	public LockProvider lockProvider(RedisConnectionFactory connectionFactory) {
		String lockEnv = env.getProperty("spring.profiles.active", "default");
		return new RedisLockProvider(connectionFactory, lockEnv);
	}
}
