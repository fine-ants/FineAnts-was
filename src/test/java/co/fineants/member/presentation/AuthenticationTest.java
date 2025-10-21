package co.fineants.member.presentation;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.global.errors.errorcode.ErrorCode;
import co.fineants.api.global.security.factory.TokenFactory;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import co.fineants.api.global.security.oauth.dto.Token;
import co.fineants.api.global.security.oauth.service.TokenService;
import co.fineants.api.global.success.MemberSuccessCode;
import co.fineants.api.global.success.OauthSuccessCode;
import co.fineants.api.global.util.ObjectMapperUtil;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.member.presentation.dto.request.LoginRequest;
import co.fineants.role.domain.Role;
import co.fineants.role.domain.RoleRepository;
import jakarta.servlet.http.Cookie;

class AuthenticationTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private TokenFactory tokenFactory;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	private Cookie[] processLogin() throws Exception {
		LoginRequest request = new LoginRequest("dragonbead95@naver.com", "nemo1234@");

		return mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(authenticated())
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(MemberSuccessCode.OK_LOGIN.getMessage())))
			.andExpect(jsonPath("data").value(nullValue()))
			.andExpect(cookie().exists("accessToken"))
			.andExpect(cookie().exists("refreshToken"))
			.andExpect(cookie().httpOnly("accessToken", true))
			.andExpect(cookie().httpOnly("refreshToken", true))
			.andExpect(cookie().secure("accessToken", true))
			.andExpect(cookie().secure("refreshToken", true))
			.andExpect(cookie().path("accessToken", "/"))
			.andExpect(cookie().path("refreshToken", "/"))
			.andReturn()
			.getResponse()
			.getCookies();
	}

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
			.apply(springSecurity())
			.alwaysDo(print())
			.build();
	}

	@DisplayName("사용자는 이메일과 비밀번호로 로그인 한다")
	@Test
	void login_whenAjaxLogin_thenAuthenticatedContext() throws Exception {
		memberRepository.save(createMember());
		processLogin();
	}

	@DisplayName("사용자는 이메일과 비밀번호가 일치하지 않아서 로그인하지 못한다")
	@Test
	void login_whenInvalidEmailAndPassword_thenUnauthenticatedContext() throws Exception {
		memberRepository.save(createMember());
		LoginRequest request = new LoginRequest("dragonbead95@naver.com", "nemo2345@");

		mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.BAD_REQUEST.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.BAD_REQUEST.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(ErrorCode.LOGIN_FAIL.getMessage())))
			.andExpect(jsonPath("data").value(nullValue()));
	}

	@DisplayName("사용자는 로그아웃한다")
	@Test
	void logout() throws Exception {
		// given
		memberRepository.save(createMember());
		Cookie[] cookies = processLogin();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/logout")
				.cookie(cookies))
			.andExpect(unauthenticated())
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(OauthSuccessCode.OK_LOGOUT.getMessage())))
			.andExpect(jsonPath("data").value(nullValue()))
			.andExpect(cookie().maxAge("accessToken", 0))
			.andExpect(cookie().maxAge("refreshToken", 0))
			.andExpect(cookie().httpOnly("accessToken", true))
			.andExpect(cookie().httpOnly("refreshToken", true))
			.andExpect(cookie().secure("accessToken", true))
			.andExpect(cookie().secure("refreshToken", true))
			.andExpect(cookie().path("accessToken", "/"))
			.andExpect(cookie().path("refreshToken", "/"));
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
	@ParameterizedTest(name = "{index} ==> the tokenCreateDate is {0}, {1} ")
	@MethodSource(value = {"co.fineants.TestDataProvider#validJwtTokenCreateDateSource"})
	void refreshAccessToken(Date accessTokenCreateDate, Date refreshTokenCreateDate) throws Exception {
		// given
		Member member = memberRepository.save(createMember());
		Set<String> roleNames = roleRepository.findAllById(member.getRoleIds()).stream()
			.map(Role::getRoleName)
			.collect(Collectors.toSet());
		Token token = tokenService.generateToken(MemberAuthentication.from(member, roleNames), accessTokenCreateDate);
		ResponseCookie accessTokenCookie = tokenFactory.createAccessTokenCookie(token);

		token = tokenService.generateToken(MemberAuthentication.from(member, roleNames), refreshTokenCreateDate);
		ResponseCookie refreshTokenCookie = tokenFactory.createRefreshTokenCookie(token);

		// when & then
		Cookie[] cookieArray = Stream.of(accessTokenCookie, refreshTokenCookie)
			.map(cookie -> new Cookie(cookie.getName(), cookie.getValue()))
			.toArray(Cookie[]::new);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/profile")
				.cookie(cookieArray))
			.andExpect(authenticated())
			.andExpect(status().isOk());
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
	@ParameterizedTest(name = "{index} ==> the tokenCreateDate is {0}, {1} ")
	@MethodSource(value = {"co.fineants.TestDataProvider#invalidJwtTokenCreateDateSource"})
	void refreshAccessToken_whenExpiredRefreshToken_then401(Date accessTokenCreateDate,
		Date refreshTokenCreateDate) throws
		Exception {
		// given
		Member member = memberRepository.save(createMember());
		Set<String> roleNames = roleRepository.findAllById(member.getRoleIds()).stream()
			.map(Role::getRoleName)
			.collect(Collectors.toSet());
		Token token = tokenService.generateToken(MemberAuthentication.from(member, roleNames), accessTokenCreateDate);
		ResponseCookie accessTokenCookie = tokenFactory.createAccessTokenCookie(token);

		token = tokenService.generateToken(MemberAuthentication.from(member, roleNames), refreshTokenCreateDate);
		ResponseCookie refreshTokenCookie = tokenFactory.createRefreshTokenCookie(token);

		// when & then
		Cookie[] cookieArray = Stream.of(accessTokenCookie, refreshTokenCookie)
			.map(cookie -> new Cookie(cookie.getName(), cookie.getValue()))
			.toArray(Cookie[]::new);
		mockMvc.perform(MockMvcRequestBuilders.get("/api/profile")
				.cookie(cookieArray))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.UNAUTHORIZED.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.UNAUTHORIZED.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(ErrorCode.UNAUTHORIZED.getMessage())))
			.andExpect(jsonPath("data").value(nullValue()));
	}
}
