package co.fineants.api.domain.exchangerate.client;

import java.util.Map;

import org.springframework.web.reactive.function.client.WebClient;

import co.fineants.api.domain.exchangerate.domain.dto.response.ExchangeRateFetchResponse;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class ExchangeRateClientHelper {
	private final WebClient webClient;

	public ExchangeRateClientHelper(WebClient webClient) {
		this.webClient = webClient;
	}

	public Mono<ExchangeRateFetchResponse> get(String path, Map<String, String> queryParams) {
		return Mono.defer(() -> {
			long startTime = System.currentTimeMillis(); // 시작 시간 기록

			return webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.path(path);
					queryParams.forEach(uriBuilder::queryParam);
					return uriBuilder.build();
				})
				.retrieve()
				.bodyToMono(ExchangeRateFetchResponse.class)
				.doOnSuccess(response -> {
					long duration = System.currentTimeMillis() - startTime; // 소요 시간 계산
					log.info("API 호출 성공 - Path: {}, 소요 시간: {}ms", path, duration);
				})
				.doOnError(error -> {
					long duration = System.currentTimeMillis() - startTime; // 소요 시간 계산
					log.error("API 호출 실패 - Path: {}, 소요 시간: {}ms, 오류: {}", path, duration, error.getMessage());
				});
		});

	}
}
