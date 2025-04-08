package co.fineants.api.domain.exchangerate.client;

import java.util.function.Function;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import co.fineants.api.global.errors.exception.business.ExternalApiGetRequestException;
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
	
	private <T> Function<ClientResponse, Mono<T>> getClientResponseMonoFunction(Class<T> responseType) {
		return clientResponse -> {
			log.info("statusCode : {}", clientResponse.statusCode());
			if (clientResponse.statusCode().is4xxClientError() || clientResponse.statusCode().is5xxServerError()) {
				return clientResponse.bodyToMono(String.class).handle((body, sink) -> {
					log.info("responseBody : {}", body);
					HttpStatus httpStatus = HttpStatus.valueOf(clientResponse.statusCode().value());
					sink.error(new ExternalApiGetRequestException(body, httpStatus));
				});
			}
			return clientResponse.bodyToMono(responseType);
		};
	}
}
