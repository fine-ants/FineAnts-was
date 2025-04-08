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
		return webClient.get()
			.uri(uriBuilder -> {
				uriBuilder.path(path);
				queryParams.forEach(uriBuilder::queryParam);
				return uriBuilder.build();
			})
			.retrieve()
			.bodyToMono(ExchangeRateFetchResponse.class);
	}
}
