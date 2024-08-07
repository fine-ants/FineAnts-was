package codesquad.fineants.domain.kis.client;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.util.Strings;
import org.assertj.core.groups.Tuple;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.kis.domain.dto.response.KisDividendWrapper;
import codesquad.fineants.domain.kis.domain.dto.response.KisIpo;
import codesquad.fineants.domain.kis.domain.dto.response.KisIpoResponse;
import codesquad.fineants.domain.kis.domain.dto.response.KisSearchStockInfo;
import codesquad.fineants.domain.kis.properties.OauthKisProperties;
import codesquad.fineants.global.util.ObjectMapperUtil;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class KisClientTest extends AbstractContainerBaseTest {
	public static MockWebServer mockWebServer;

	private KisClient kisClient;

	@BeforeAll
	static void setUp() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();
	}

	@AfterAll
	static void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@BeforeEach
	void initialize() {
		OauthKisProperties oauthKisProperties = new OauthKisProperties(
			"appkey",
			"secertkey",
			"otkenURI",
			"currentPriceURI",
			"lastDayClosingPriceURI",
			"dividendURI",
			"ipoURI",
			"searchStockInfoURI"
		);
		String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
		this.kisClient = new KisClient(
			oauthKisProperties,
			WebClient.builder().baseUrl(baseUrl).build(),
			WebClient.builder().baseUrl(baseUrl).build());
	}

	@DisplayName("한국투자증권 서버로부터 액세스 토큰 발급이 한번 실패하는 경우 재발급을 다시 요청한다")
	@Test
	void accessToken_whenIssueAccessToken_thenRetryOnAccessTokenFailure() {
		// given
		KisAccessToken expectedKisAccessToken = createKisAccessToken();
		mockWebServer.enqueue(createResponse(403, ObjectMapperUtil.serialize(createError())));
		mockWebServer.enqueue(createResponse(200, ObjectMapperUtil.serialize(expectedKisAccessToken)));

		// when
		Mono<KisAccessToken> responseMono = this.kisClient.fetchAccessToken();

		// then
		StepVerifier
			.withVirtualTime(() -> responseMono)
			.expectSubscription()
			.thenAwait(Duration.ofMinutes(1))
			.expectNextMatches(expectedKisAccessToken::equals)
			.expectComplete()
			.verify();
	}

	@DisplayName("어제부터 오늘까지 상장된 종목들을 조회한다")
	@Test
	void fetchIpo() {
		// given
		Map<String, Object> responseBodyMap = new HashMap<>();
		List<Map<String, String>> output1 = new ArrayList<>();
		Map<String, String> stock1 = new HashMap<>();
		stock1.put("list_dt", "20240326");
		stock1.put("sht_cd", "034220");
		stock1.put("isin_name", "LG디스플레이");
		stock1.put("stk_kind", "보통");
		stock1.put("issue_type", "유상증자");
		stock1.put("issue_stk_qty", "142184300");
		stock1.put("tot_issue_stk_qty", "500000000");
		stock1.put("issue_price", "9090");
		output1.add(stock1);
		responseBodyMap.put("output1", output1);
		mockWebServer.enqueue(createResponse(200, ObjectMapperUtil.serialize(responseBodyMap)));

		LocalDate today = LocalDate.now();
		LocalDate yesterday = today.minusDays(1);
		KisAccessToken kisAccessToken = createKisAccessToken();
		// when
		KisIpoResponse response = kisClient.fetchIpo(yesterday, today, kisAccessToken.createAuthorization())
			.block(Duration.ofSeconds(1));

		// then
		assertAll(
			() -> assertThat(response).isNotNull(),
			() -> assertThat(Objects.requireNonNull(response).getKisIpos())
				.hasSize(1)
				.extracting(KisIpo::getListDt, KisIpo::getShtCd, KisIpo::getIsinName)
				.containsExactlyInAnyOrder(Tuple.tuple("20240326", "034220", "LG디스플레이"))
		);
	}

	@DisplayName("서버는 첫 요청시 초당 거래 횟수를 초과하여 두번째 재요청에 응답을 받는다")
	@Test
	void fetchIpo_whenOverLimit_thenRetryAndReceiveResponse() {
		// given
		Map<String, String> badRequestBody = new HashMap<>();
		badRequestBody.put("rt_cd", "1");
		badRequestBody.put("msg_cd", "EGW00201");
		badRequestBody.put("msg1", "초당 거래건수를 초과하였습니다.");
		mockWebServer.enqueue(createResponse(400, ObjectMapperUtil.serialize(badRequestBody)));

		Map<String, Object> okResponseBody = new HashMap<>();
		List<Map<String, String>> output1 = new ArrayList<>();
		Map<String, String> stock1 = new HashMap<>();
		stock1.put("list_dt", "20240326");
		stock1.put("sht_cd", "034220");
		stock1.put("isin_name", "LG디스플레이");
		stock1.put("stk_kind", "보통");
		stock1.put("issue_type", "유상증자");
		stock1.put("issue_stk_qty", "142184300");
		stock1.put("tot_issue_stk_qty", "500000000");
		stock1.put("issue_price", "9090");
		output1.add(stock1);
		okResponseBody.put("output1", output1);
		mockWebServer.enqueue(createResponse(200, ObjectMapperUtil.serialize(okResponseBody)));

		LocalDate today = LocalDate.now();
		LocalDate yesterday = today.minusDays(1);
		KisAccessToken kisAccessToken = createKisAccessToken();
		// when
		KisIpoResponse response = kisClient.fetchIpo(yesterday, today, kisAccessToken.createAuthorization())
			.blockOptional(Duration.ofMinutes(10))
			.orElseThrow();

		// then
		assertThat(response).isNotNull();
		assertThat(response.getKisIpos()).hasSize(1);
	}

	@DisplayName("서버는 한국투자증권 서버에 요청하여 종목의 상세 정보를 조회한다")
	@Test
	void fetchSearchStockInfo() {
		// given
		String tickerSymbol = "034220";
		String accessToken = createKisAccessToken().getAccessToken();

		Map<String, Object> okResponseBody = new HashMap<>();
		KisSearchStockInfo output = KisSearchStockInfo.listedStock(
			"KR7000660001",
			"00000A000660",
			"에스케이하이닉스보통주",
			"SK hynix",
			"STK",
			"전기,전자"
		);
		okResponseBody.put("output", output);

		mockWebServer.enqueue(createResponse(200, ObjectMapperUtil.serialize(okResponseBody)));
		// when
		KisSearchStockInfo kisSearchStockInfo = kisClient.fetchSearchStockInfo(tickerSymbol, accessToken)
			.blockOptional(Duration.ofMinutes(10))
			.orElse(null);
		// then
		assertThat(kisSearchStockInfo).isNotNull();
		assertThat(kisSearchStockInfo).extracting(
			KisSearchStockInfo::getStockCode,
			KisSearchStockInfo::getTickerSymbol,
			KisSearchStockInfo::getCompanyName,
			KisSearchStockInfo::getCompanyEngName,
			KisSearchStockInfo::getMarketIdCode,
			KisSearchStockInfo::getSector
		).containsExactly("KR7000660001", "000660", "에스케이하이닉스보통주", "SK hynix", "STK", "전기,전자");
	}

	@DisplayName("서버는 한국투자증권 서버에 요청하여 첫번째 요청은 실패하고 두번째 요청에 응답을 받는다")
	@Test
	void fetchSearchStockInfo_whenOverLimit_thenRetryRequest() {
		// given
		Map<String, String> badRequestBody = new HashMap<>();
		badRequestBody.put("rt_cd", "1");
		badRequestBody.put("msg_cd", "EGW00201");
		badRequestBody.put("msg1", "초당 거래건수를 초과하였습니다.");
		mockWebServer.enqueue(createResponse(400, ObjectMapperUtil.serialize(badRequestBody)));

		Map<String, Object> okResponseBody = new HashMap<>();
		KisSearchStockInfo output = KisSearchStockInfo.listedStock(
			"KR7000660001",
			"00000A000660",
			"에스케이하이닉스보통주",
			"SK hynix",
			"STK",
			"전기,전자"
		);
		okResponseBody.put("output", output);
		mockWebServer.enqueue(createResponse(200, ObjectMapperUtil.serialize(okResponseBody)));

		String tickerSymbol = "034220";
		String accessToken = createKisAccessToken().getAccessToken();
		// when
		KisSearchStockInfo kisSearchStockInfo = kisClient.fetchSearchStockInfo(tickerSymbol, accessToken)
			.retry(1)
			.blockOptional()
			.orElse(null);
		// then
		assertThat(kisSearchStockInfo).isNotNull();
		assertThat(kisSearchStockInfo).extracting(
			KisSearchStockInfo::getStockCode,
			KisSearchStockInfo::getTickerSymbol,
			KisSearchStockInfo::getCompanyName,
			KisSearchStockInfo::getCompanyEngName,
			KisSearchStockInfo::getMarketIdCode,
			KisSearchStockInfo::getSector
		).containsExactly("KR7000660001", "000660", "에스케이하이닉스보통주", "SK hynix", "STK", "전기,전자");
	}

	@DisplayName("사용자는 종목의 배당 일정을 조회한다")
	@Test
	void fetchDividend() {
		// given
		String tickerSymbol = "000720";
		KisAccessToken kisAccessToken = createKisAccessToken();
		Map<String, Object> output = Map.ofEntries(
			Map.entry("output1",
				List.of(
					Map.of("sht_cd", "000720",
						"per_sto_divi_amt", "000000000600",
						"record_date", "20240326",
						"divi_pay_dt", Strings.EMPTY),
					Map.of("sht_cd", "000720",
						"per_sto_divi_amt", "000000000600",
						"record_date", "20240630",
						"divi_pay_dt", Strings.EMPTY)
				)
			)
		);
		mockWebServer.enqueue(createResponse(200, ObjectMapperUtil.serialize(output)));
		// when
		Mono<KisDividendWrapper> mono = kisClient.fetchDividendThisYear(tickerSymbol,
			kisAccessToken.createAuthorization());
		KisDividendWrapper wrapper = mono
			.blockOptional(Duration.ofSeconds(1)).orElse(null);
		// then
		assertThat(wrapper).isNotNull();
		assertThat(wrapper.getKisDividends()).hasSize(2);
	}

	@NotNull
	private static MockResponse createResponse(int code, String body) {
		return new MockResponse()
			.setResponseCode(code)
			.setBody(body)
			.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
	}

	private Map<String, String> createError() {
		Map<String, String> responseBody = new HashMap<>();
		responseBody.put("error_description", "접근토큰 발급 잠시 후 다시 시도하세요(1분당 1회)");
		responseBody.put("error_code", "EGW00133");
		return responseBody;
	}

	public KisAccessToken createKisAccessToken() {
		return new KisAccessToken(
			"accessToken",
			"Bearer",
			LocalDateTime.of(2023, 12, 7, 11, 41, 27),
			86400
		);
	}
}

