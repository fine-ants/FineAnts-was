package co.fineants.api.domain.exchangerate.client;

import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class WebClientWrapper {
	private final WebClient webClient;

	public WebClientWrapper() {
		this.webClient = WebClient.builder()
			.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
			.build();
	}

	public <T> Mono<T> get(String uri, MultiValueMap<String, String> headerMap, Class<T> responseType) {
		return webClient.get()
			.uri(uri)
			.headers(header -> header.addAll(headerMap))
			.retrieve()
			.bodyToMono(responseType);
	}
}
