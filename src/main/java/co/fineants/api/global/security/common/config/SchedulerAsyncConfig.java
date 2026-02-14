package co.fineants.api.global.security.common.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

@Configuration
@EnableAsync
public class SchedulerAsyncConfig {

	@Bean
	public Executor schedulerAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		// 1. 스레드 이름 접두사 설정
		executor.setThreadNamePrefix("scheduler-async-executor-");
		// 2. 스레드풀 상세 설정
		executor.setCorePoolSize(5); // 기본 유지 스레드 수
		executor.setMaxPoolSize(10); // 최대 스레드 수
		executor.setQueueCapacity(100); // 대기 큐 크기
		// 3. 스레드 풀 종료 시 작업 완료 대기 설정
		executor.setAwaitTerminationSeconds(60);

		executor.initialize();
		return new DelegatingSecurityContextAsyncTaskExecutor(executor);
	}

}
