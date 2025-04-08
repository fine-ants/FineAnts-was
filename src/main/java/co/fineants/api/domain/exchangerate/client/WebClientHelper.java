package co.fineants.api.domain.exchangerate.client;

import java.util.Map;

import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class WebClientHelper {
	private final WebClient webClient;

	public WebClientHelper(WebClient webClient) {
		this.webClient = webClient;
	}

	public <T> Mono<T> get(String path, Map<String, String> queryParams, Class<T> responseType) {
		return webClient.get()
			.uri(uriBuilder -> {
				uriBuilder.path(path);
				queryParams.forEach(uriBuilder::queryParam);
				return uriBuilder.build();
			})
			.retrieve()
			.bodyToMono(responseType);
	}
}
