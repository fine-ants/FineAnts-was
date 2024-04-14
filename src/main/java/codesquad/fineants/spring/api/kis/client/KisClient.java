package codesquad.fineants.spring.api.kis.client;

import static codesquad.fineants.spring.api.kis.service.KisService.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import codesquad.fineants.spring.api.common.errors.exception.KisException;
import codesquad.fineants.spring.api.kis.properties.OauthKisProperties;
import codesquad.fineants.spring.api.kis.response.KisClosingPrice;
import codesquad.fineants.spring.api.kis.response.KisDividend;
import codesquad.fineants.spring.api.kis.response.KisDividendWrapper;
import codesquad.fineants.spring.util.ObjectMapperUtil;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@Component
public class KisClient {

	private final WebClient webClient;
	private final WebClient realWebClient;
	private final OauthKisProperties oauthKisProperties;

	public KisClient(OauthKisProperties properties,
		@Qualifier(value = "kisWebClient") WebClient webClient,
		@Qualifier(value = "realKisWebClient") WebClient realWebClient) {
		this.webClient = webClient;
		this.oauthKisProperties = properties;
		this.realWebClient = realWebClient;
	}

	// 액세스 토큰 발급
	public Mono<KisAccessToken> fetchAccessToken() {
		Map<String, String> requestBodyMap = new HashMap<>();
		requestBodyMap.put("grant_type", "client_credentials");
		requestBodyMap.put("appkey", oauthKisProperties.getAppkey());
		requestBodyMap.put("appsecret", oauthKisProperties.getSecretkey());
		return webClient
			.post()
			.uri(oauthKisProperties.getTokenURI())
			.bodyValue(requestBodyMap)
			.retrieve()
			.onStatus(HttpStatus::isError, this::handleError)
			.bodyToMono(KisAccessToken.class)
			.retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofMinutes(1)))
			.log();
	}

	// 현재가 조회
	public Mono<KisCurrentPrice> fetchCurrentPrice(String tickerSymbol, String authorization) {
		MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
		headerMap.add("authorization", authorization);
		headerMap.add("appkey", oauthKisProperties.getAppkey());
		headerMap.add("appsecret", oauthKisProperties.getSecretkey());
		headerMap.add("tr_id", "FHKST01010100");

		MultiValueMap<String, String> queryParamMap = new LinkedMultiValueMap<>();
		queryParamMap.add("fid_cond_mrkt_div_code", "J");
		queryParamMap.add("fid_input_iscd", tickerSymbol);

		return performGet(
			oauthKisProperties.getCurrentPriceURI(),
			headerMap,
			queryParamMap,
			KisCurrentPrice.class
		);
	}

	// 직전 거래일의 종가 조회
	public Mono<KisClosingPrice> fetchClosingPrice(String tickerSymbol, String authorization) {
		MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
		headerMap.add("authorization", authorization);
		headerMap.add("appkey", oauthKisProperties.getAppkey());
		headerMap.add("appsecret", oauthKisProperties.getSecretkey());
		headerMap.add("tr_id", "FHKST03010100");

		MultiValueMap<String, String> queryParamMap = new LinkedMultiValueMap<>();
		queryParamMap.add("FID_COND_MRKT_DIV_CODE", "J");
		queryParamMap.add("FID_INPUT_ISCD", tickerSymbol);
		queryParamMap.add("FID_INPUT_DATE_1", LocalDate.now().minusDays(1L).toString());
		queryParamMap.add("FID_INPUT_DATE_2", LocalDate.now().minusDays(1L).toString());
		queryParamMap.add("FID_PERIOD_DIV_CODE", "D");
		queryParamMap.add("FID_ORG_ADJ_PRC", "0");

		return performGet(
			oauthKisProperties.getLastDayClosingPriceURI(),
			headerMap,
			queryParamMap,
			KisClosingPrice.class
		);
	}

	// 배당금 조회
	public String fetchDividend(String tickerSymbol, String authorization) {
		MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
		headerMap.add("content-type", "application/json; charset=utf-8");
		headerMap.add("authorization", authorization);
		headerMap.add("appkey", oauthKisProperties.getAppkey());
		headerMap.add("appsecret", oauthKisProperties.getSecretkey());
		headerMap.add("tr_id", "HHKDB669102C0");
		headerMap.add("custtype", "P");

		MultiValueMap<String, String> queryParamMap = new LinkedMultiValueMap<>();
		queryParamMap.add("HIGH_GB", Strings.EMPTY);
		queryParamMap.add("CTS", Strings.EMPTY);
		queryParamMap.add("GB1", "0");
		queryParamMap.add("F_DT", "20230101");
		queryParamMap.add("T_DT", "20231231");
		queryParamMap.add("SHT_CD", tickerSymbol);

		return performGet(
			oauthKisProperties.getDividendURI(),
			headerMap,
			queryParamMap,
			String.class,
			realWebClient
		).block();
	}

	public List<KisDividend> fetchDividend(LocalDate from, LocalDate to, String authorization) {
		MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
		headerMap.add("content-type", "application/json; charset=utf-8");
		headerMap.add("authorization", authorization);
		headerMap.add("appkey", oauthKisProperties.getAppkey());
		headerMap.add("appsecret", oauthKisProperties.getSecretkey());
		headerMap.add("tr_id", "HHKDB669102C0");
		headerMap.add("custtype", "P");

		MultiValueMap<String, String> queryParamMap = new LinkedMultiValueMap<>();
		queryParamMap.add("HIGH_GB", Strings.EMPTY);
		queryParamMap.add("CTS", Strings.EMPTY);
		queryParamMap.add("GB1", "0");
		queryParamMap.add("F_DT", from.format(DateTimeFormatter.BASIC_ISO_DATE));
		queryParamMap.add("T_DT", to.format(DateTimeFormatter.BASIC_ISO_DATE));
		queryParamMap.add("SHT_CD", Strings.EMPTY);

		Mono<String> jsonMono = performGet(
			oauthKisProperties.getDividendURI(),
			headerMap,
			queryParamMap,
			String.class,
			realWebClient
		);

		return jsonMono.map(json -> {
			KisDividendWrapper wrapper = ObjectMapperUtil.deserialize(json, KisDividendWrapper.class);
			return wrapper.getKisDividends();
		}).block(TIMEOUT);
	}

	private <T> Mono<T> performGet(String uri, MultiValueMap<String, String> headerMap,
		MultiValueMap<String, String> queryParamMap, Class<T> responseType) {
		return performGet(uri, headerMap, queryParamMap, responseType, webClient);
	}

	private <T> Mono<T> performGet(String uri, MultiValueMap<String, String> headerMap,
		MultiValueMap<String, String> queryParamMap, Class<T> responseType, WebClient webClient) {
		return webClient
			.get()
			.uri(uriBuilder -> uriBuilder
				.path(uri)
				.queryParams(queryParamMap)
				.build())
			.headers(httpHeaders -> httpHeaders.addAll(headerMap))
			.retrieve()
			.onStatus(HttpStatus::isError, this::handleError)
			.bodyToMono(responseType)
			.retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5)));
	}

	private Mono<? extends Throwable> handleError(ClientResponse clientResponse) {
		return clientResponse.bodyToMono(String.class)
			.doOnNext(log::info)
			.flatMap(body -> Mono.error(() -> new KisException(body)));
	}
}
