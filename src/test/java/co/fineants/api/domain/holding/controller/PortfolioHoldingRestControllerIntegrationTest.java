package co.fineants.api.domain.holding.controller;

import static co.fineants.api.global.success.PortfolioStockSuccessCode.*;
import static org.hamcrest.Matchers.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.MemberRole;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.security.ajax.token.AjaxAuthenticationToken;
import co.fineants.api.global.security.factory.TokenFactory;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import co.fineants.api.global.security.oauth.dto.Token;
import co.fineants.api.global.security.oauth.service.TokenService;
import co.fineants.api.global.util.ObjectMapperUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;

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

	@BeforeEach
	void setUp() {
		RestAssured.port = port;
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
	}

	// todo: 인증 관련 설정 필요함
	@Test
	void createPortfolioHolding() {
		Map<String, Object> purchaseHistoryMap = new HashMap<>();
		purchaseHistoryMap.put("purchaseDate", LocalDateTime.now().toString());
		purchaseHistoryMap.put("numShares", 10L);
		purchaseHistoryMap.put("purchasePricePerShare", 100.0);
		purchaseHistoryMap.put("memo", null);

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("tickerSymbol", "005930");
		requestBodyMap.put("purchaseHistory", purchaseHistoryMap);

		String body = ObjectMapperUtil.serialize(requestBodyMap);

		Member member = memberRepository.save(createMember());
		Token token = tokenService.generateToken(MemberAuthentication.from(member), new Date());
		Cookie accessTokenCookie = getRestAssuredCookie(tokenFactory.createAccessTokenCookie(token));
		Cookie refreshTokenCookie = getRestAssuredCookie(tokenFactory.createRefreshTokenCookie(token));

		List<GrantedAuthority> authorities = member.getRoles().stream()
			.map(MemberRole::toSimpleGrantedAuthority)
			.toList();
		SecurityContextHolder.getContext()
			.setAuthentication(AjaxAuthenticationToken.authenticated(member, null, authorities));

		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		stockRepository.save(createSamsungStock());

		RestAssured.given()
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
			.body("message", equalTo(CREATED_ADD_PORTFOLIO_STOCK.getMessage()))
			.body("data.portfolioHoldingId", notNullValue());
	}

	private Cookie getRestAssuredCookie(ResponseCookie cookie) {
		return new Cookie.Builder(cookie.getName(), cookie.getValue())
			.setDomain(cookie.getDomain())
			.setPath(cookie.getPath())
			.setHttpOnly(cookie.isHttpOnly())
			.setSecured(cookie.isSecure())
			.setSameSite(cookie.getSameSite())
			.build();
	}

	private MemberAuthentication createMemberAuthentication() {
		return MemberAuthentication.create(
			1L,
			"dragonbead95@naver.com",
			"일개미1234",
			"local",
			"profileUrl",
			Set.of("ROLE_USER")
		);
	}
}
