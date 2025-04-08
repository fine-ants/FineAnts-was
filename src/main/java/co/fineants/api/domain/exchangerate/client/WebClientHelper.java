package co.fineants.api.domain.exchangerate.client;

import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class WebClientHelper {
	private final WebClient webClient;

	public WebClientHelper(WebClient webClient) {
		this.webClient = webClient;
	}

	public <T> Mono<T> get(String path, String base, Class<T> responseType) {
		return webClient.get()
			.uri(uriBuilder -> uriBuilder.path(path).queryParam("base", base).build())
			.retrieve()
			.bodyToMono(responseType);
	}
}
