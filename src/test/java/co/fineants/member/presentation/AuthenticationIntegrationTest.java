package co.fineants.member.presentation;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.global.security.factory.TokenFactory;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import co.fineants.api.global.security.oauth.dto.Token;
import co.fineants.api.global.security.oauth.service.TokenService;
import co.fineants.api.global.util.ObjectMapperUtil;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.role.domain.Role;
import co.fineants.role.domain.RoleRepository;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

public class AuthenticationIntegrationTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private TokenFactory tokenFactory;

	@Autowired
	private RoleRepository roleRepository;

	@LocalServerPort
	private int port;

	public static Stream<Arguments> validJwtTokenCreateDateSource() {
		Date now = new Date();
		long oneDayMilliSeconds = 1000 * 60 * 60 * 24; // 1일
		long oneHourMilliSeconds = 1000 * 60 * 60; // 1시간
		long oneMinuteMilliSeconds = 1000 * 60; // 1분
		long thirteenDaysMilliSeconds =
			oneDayMilliSeconds * 13 + oneHourMilliSeconds * 23 + oneMinuteMilliSeconds * 5; // 13일 23시간 5분
		Date now1 = new Date(now.getTime() - oneDayMilliSeconds);
		Date now2 = new Date(now.getTime() - thirteenDaysMilliSeconds);

		return Stream.of(
			Arguments.of(now1, now1),
			Arguments.of(now2, now2),
			Arguments.of(now, now2)
		);
	}

	public static Stream<Arguments> invalidJwtTokenCreateDateSource() {
		long fifteenDayMilliSeconds = 1000 * 60 * 60 * 24 * 15; // 1일
		Date now1 = new Date(fifteenDayMilliSeconds);
		return Stream.of(
			Arguments.of(now1, now1)
		);
	}

	@BeforeEach
	void setUp() {
		RestAssured.port = port;
	}

	@SuppressWarnings("checkstyle:NoWhitespaceBefore")
	@DisplayName("사용자는 일반 로그인한다")
	@Test
	void login() {
		// given
		memberRepository.save(createMember());

		Map<String, String> body = Map.of(
			"email", "dragonbead95@naver.com",
			"password", "nemo1234@"
		);
		String json = ObjectMapperUtil.serialize(body);
		// when & then
		given().log().all()
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.body(json)
			.when()
			.post("/api/auth/login")
			.then()
			.cookie("accessToken", notNullValue())
			.cookie("refreshToken", notNullValue())
			.log()
			.body()
			.statusCode(200)
			.assertThat()
			.body("data", Matchers.nullValue());
	}

	@DisplayName("사용자는 이메일 또는 비밀번호를 틀려서 로그인 할 수 없다")
	@Test
	void login_whenInvalidUsernameAndPassword_then401() {
		// given
		memberRepository.save(createMember());

		Map<String, String> body = Map.of(
			"email", "user1@gmail.com",
			"password", "user1@"
		);
		String json = ObjectMapperUtil.serialize(body);
		// when & then
		given().log().all()
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.body(json)
			.when()
			.post("/api/auth/login")
			.then()
			.log()
			.body()
			.statusCode(400);
	}

	@DisplayName("사용자는 로그아웃한다")
	@Test
	void logout() {
		// given
		memberRepository.save(createMember());
		Map<String, String> cookies = processLogin();

		String url = "/api/auth/logout";
		// when
		given().log().all()
			.cookies(cookies)
			.when()
			.get(url)
			.then()
			.log()
			.body()
			.statusCode(200);

		// then
		given().log().all()
			.cookies(cookies)
			.when()
			.get("/api/profile")
			.then()
			.log()
			.body()
			.statusCode(401);
	}

	private Map<String, String> processLogin() {
		Map<String, String> body = Map.of(
			"email", "dragonbead95@naver.com",
			"password", "nemo1234@"
		);
		String json = ObjectMapperUtil.serialize(body);
		ExtractableResponse<Response> extract = given()
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.body(json)
			.when()
			.post("/api/auth/login")
			.then()
			.log()
			.body()
			.statusCode(200)
			.extract();
		return extract.cookies();
	}

	/**
	 * 토큰 갱신 테스트
	 * <p>
	 * 토큰 갱신 성공 케이스
	 * - accessToken 만료 and refreshToken 유효 => accessToken 갱신
	 * - accessToken 만료 and refreshToken 만료 임박 => accessToken 갱신, refreshToken 갱신
	 * - accessToken 유효 and refreshToken 만료 임박 => refreshToken 갱신
	 *
	 * @param accessTokenCreateDate AccessToken 생성 시간
	 * @param refreshTokenCreateDate RefreshToken 생성 시간
	 */
	@DisplayName("사용자는 액세스 토큰이 만료된 상태에서 액세스 토큰을 갱신한다")
	@MethodSource(value = {"validJwtTokenCreateDateSource"})
	@ParameterizedTest(name = "{index} ==> the tokenCreateDate is {0}, {1} ")
	void refreshAccessToken(Date accessTokenCreateDate, Date refreshTokenCreateDate) {
		// given
		Member member = memberRepository.save(createMember());
		Set<String> roleNames = roleRepository.findAllById(member.getRoleIds()).stream()
			.map(Role::getRoleName)
			.collect(Collectors.toSet());
		Token token = tokenService.generateToken(MemberAuthentication.from(member, roleNames), accessTokenCreateDate);
		ResponseCookie accessTokenCookie = tokenFactory.createAccessTokenCookie(token);

		token = tokenService.generateToken(MemberAuthentication.from(member, roleNames), refreshTokenCreateDate);
		ResponseCookie refreshTokenCookie = tokenFactory.createRefreshTokenCookie(token);

		Map<String, String> cookies = Map.of(
			accessTokenCookie.getName(), accessTokenCookie.getValue(),
			refreshTokenCookie.getName(), refreshTokenCookie.getValue()
		);

		// when & then
		given().log().all()
			.cookies(cookies)
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.when()
			.get("/api/profile")
			.then()
			.cookies("accessToken", notNullValue())
			.cookies("refreshToken", notNullValue())
			.log()
			.body()
			.statusCode(200);
	}

	/**
	 * 토큰 갱신 실패 테스트
	 * <p>
	 *
	 * 토큰 갱신 실패 케이스
	 * - accessToken 만료 and refreshToken 만료 => 401
	 *
	 * @param accessTokenCreateDate AccessToken 생성 시간
	 * @param refreshTokenCreateDate RefreshToken 생성 시간
	 */
	@DisplayName("사용자는 리프레시 토큰이 만료된 상태에서는 액세스 토큰을 갱신할 수 없다")
	@MethodSource(value = {"invalidJwtTokenCreateDateSource"})
	@ParameterizedTest(name = "{index} ==> the tokenCreateDate is {0}, {1} ")
	void refreshAccessToken_whenExpiredRefreshToken_then401(Date accessTokenCreateDate, Date refreshTokenCreateDate) {
		// given
		Member member = memberRepository.save(createMember());
		Set<String> roleNames = roleRepository.findAllById(member.getRoleIds()).stream()
			.map(Role::getRoleName)
			.collect(Collectors.toSet());
		Token token = tokenService.generateToken(MemberAuthentication.from(member, roleNames), accessTokenCreateDate);
		ResponseCookie accessTokenCookie = tokenFactory.createAccessTokenCookie(token);

		token = tokenService.generateToken(MemberAuthentication.from(member, roleNames), refreshTokenCreateDate);
		ResponseCookie refreshTokenCookie = tokenFactory.createRefreshTokenCookie(token);

		Map<String, String> cookies = Map.of(
			accessTokenCookie.getName(), accessTokenCookie.getValue(),
			refreshTokenCookie.getName(), refreshTokenCookie.getValue()
		);

		// when & then
		given().log().all()
			.cookies(cookies)
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.when()
			.get("/api/profile")
			.then()
			.log()
			.body()
			.statusCode(401);
	}
}
