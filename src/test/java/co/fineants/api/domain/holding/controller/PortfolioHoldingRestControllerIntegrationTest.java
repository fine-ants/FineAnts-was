package co.fineants.api.domain.holding.controller;

import static co.fineants.api.global.success.PortfolioHoldingSuccessCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.security.factory.TokenFactory;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import co.fineants.api.global.security.oauth.dto.Token;
import co.fineants.api.global.security.oauth.service.TokenService;
import co.fineants.api.global.util.ObjectMapperUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class PortfolioHoldingRestControllerIntegrationTest extends AbstractContainerBaseTest {

	@LocalServerPort
	private int port;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private TokenFactory tokenFactory;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private PortfolioHoldingRepository portfolioHoldingRepository;

	@Autowired
	private PurchaseHistoryRepository purchaseHistoryRepository;
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	private Cookie accessTokenCookie;
	private Cookie refreshTokenCookie;
	private Portfolio portfolio;
	private Stock samsung;

	private Cookie getRestAssuredCookie(ResponseCookie cookie) {
		return new Cookie.Builder(cookie.getName(), cookie.getValue())
			.setDomain(cookie.getDomain())
			.setPath(cookie.getPath())
			.setHttpOnly(cookie.isHttpOnly())
			.setSecured(cookie.isSecure())
			.setSameSite(cookie.getSameSite())
			.build();
	}

	@NotNull
	private Map<String, Object> getPortfolioHoldingCreateRequestBodyMap() {
		Map<String, Object> purchaseHistoryMap = new HashMap<>();
		purchaseHistoryMap.put("purchaseDate", LocalDateTime.now().toString());
		purchaseHistoryMap.put("numShares", 10L);
		purchaseHistoryMap.put("purchasePricePerShare", 100.0);
		purchaseHistoryMap.put("memo", "memo");

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("tickerSymbol", "005930");
		requestBodyMap.put("purchaseHistory", purchaseHistoryMap);
		return requestBodyMap;
	}

	@BeforeEach
	void setUp() {
		RestAssured.port = port;
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

		// 테스트 데이터 생성
		Member member = memberRepository.save(createMember());
		portfolio = portfolioRepository.save(createPortfolio(member));
		samsung = stockRepository.save(createSamsungStock());

		// 인증 정보 설정
		setAuthentication(member);

		// 토큰 생성 및 쿠키 설정
		Token token = tokenService.generateToken(MemberAuthentication.from(member), new Date());
		accessTokenCookie = getRestAssuredCookie(tokenFactory.createAccessTokenCookie(token));
		refreshTokenCookie = getRestAssuredCookie(tokenFactory.createRefreshTokenCookie(token));
	}

	@DisplayName("포트폴리오 종목 및 매입이력 생성")
	@Test
	void createPortfolioHolding() {
		String body = ObjectMapperUtil.serialize(getPortfolioHoldingCreateRequestBodyMap());

		ExtractableResponse<Response> response = RestAssured.given()
			.contentType(ContentType.JSON)
			.cookie(accessTokenCookie)
			.cookie(refreshTokenCookie)
			.body(body)
			.when()
			.post("/api/portfolio/{portfolioId}/holdings", portfolio.getId())
			.then()
			.statusCode(HttpStatus.CREATED.value())
			.body("code", equalTo(HttpStatus.CREATED.value()))
			.body("status", equalTo(HttpStatus.CREATED.getReasonPhrase()))
			.body("message", equalTo(CREATED_ADD_PORTFOLIO_HOLDING.getMessage()))
			.body("data.portfolioHoldingId", notNullValue())
			.extract();

		Integer holdingIdInteger = response.path("data.portfolioHoldingId");
		long holdingId = holdingIdInteger.longValue();
		assertThat(portfolioHoldingRepository.findById(holdingId)).isPresent();
		assertThat(purchaseHistoryRepository.findAllByPortfolioHoldingId(holdingId)).hasSize(1);
		assertThat(redisTemplate.opsForValue().get("tickerSymbols::" + portfolio.getId())).isNotNull();
	}

	@DisplayName("같은 종목에 대하여 포트폴리오 종목 생성시 중복적으로 생성되지 않는다")
	@Test
	void createPortfolioHolding_whenPortfolioHoldingExist_thenNotSaveNewPortfolioHolding() {
		portfolioHoldingRepository.save(PortfolioHolding.of(portfolio, samsung));
		String body = ObjectMapperUtil.serialize(getPortfolioHoldingCreateRequestBodyMap());

		ExtractableResponse<Response> response = RestAssured.given()
			.contentType(ContentType.JSON)
			.cookie(accessTokenCookie)
			.cookie(refreshTokenCookie)
			.body(body)
			.when()
			.post("/api/portfolio/{portfolioId}/holdings", portfolio.getId())
			.then()
			.statusCode(HttpStatus.CREATED.value())
			.body("code", equalTo(HttpStatus.CREATED.value()))
			.body("status", equalTo(HttpStatus.CREATED.getReasonPhrase()))
			.body("message", equalTo(CREATED_ADD_PORTFOLIO_HOLDING.getMessage()))
			.body("data.portfolioHoldingId", notNullValue())
			.extract();

		Integer holdingIdInteger = response.path("data.portfolioHoldingId");
		long holdingId = holdingIdInteger.longValue();
		assertThat(portfolioHoldingRepository.findAllByPortfolio(portfolio)).hasSize(1);
		assertThat(purchaseHistoryRepository.findAllByPortfolioHoldingId(holdingId)).hasSize(1);
		assertThat(redisTemplate.opsForValue().get("tickerSymbols::" + portfolio.getId())).isNotNull();
	}

}
