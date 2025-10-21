package co.fineants.api.global.security.common.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor delegate = new ThreadPoolTaskExecutor();
		delegate.setCorePoolSize(5);
		delegate.setMaxPoolSize(10);
		delegate.setQueueCapacity(100);
		delegate.setThreadNamePrefix("async-");
		delegate.initialize();
		return new DelegatingSecurityContextAsyncTaskExecutor(delegate);
	}
}
