package co.fineants.api.global.security.oauth.filter;

import java.io.IOException;
import java.time.LocalDateTime;

import org.apache.logging.log4j.util.Strings;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import co.fineants.api.domain.member.service.RedisTokenManagementService;
import co.fineants.api.global.security.factory.TokenFactory;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import co.fineants.api.global.security.oauth.dto.Token;
import co.fineants.api.global.security.oauth.service.TokenService;
import co.fineants.api.global.util.CookieUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {
	private final TokenService tokenService;
	private final RedisTokenManagementService redisTokenManagementService;
	private final TokenFactory tokenFactory;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
		IOException,
		ServletException,
		AuthenticationException {
		String accessToken = CookieUtils.getAccessToken((HttpServletRequest)request);
		String refreshToken = CookieUtils.getRefreshToken((HttpServletRequest)request);

		// accessToken 만료 and refreshToken 유효 => accessToken 갱신
		// accessToken 만료 and refreshToken 만료 임박 => accessToken 갱신, refreshToken 갱신
		// accessToken 유효 and refreshToken 만료 임박 => accessToken 갱신, refreshToken 갱신
		// accessToken 만료 and refreshToken 만료 => 401
		// accessToken 유효 and refreshToken 만료 => nothing
		// accessToken 유효 and refreshToken 유효 => nothing
		Token token = null;
		log.debug("requestURL : {}", ((HttpServletRequest)request).getRequestURL());
		log.debug("accessToken : {}", accessToken);
		if (accessToken != null && !redisTokenManagementService.isAlreadyLogout(accessToken)) {
			if (tokenService.isExpiredToken(accessToken)) {
				log.debug("accessToken is Expired");
				token = tokenService.refreshToken(refreshToken, LocalDateTime.now());
				setTokenCookie((HttpServletResponse)response, token);
			} else if (tokenService.verifyToken(accessToken) && tokenService.isRefreshTokenNearExpiry(refreshToken)) {
				log.debug("accessToken is verified but, refreshToken is near expired");
				token = tokenService.refreshToken(refreshToken, LocalDateTime.now());
				setTokenCookie((HttpServletResponse)response, token);
			} else if (tokenService.verifyToken(accessToken)) {
				log.debug("accessToken is verified and refreshToken is not near expired");
				token = Token.create(accessToken, refreshToken);
			}
		}

		if (token != null) {
			setAuthentication(token.getAccessToken());
		}

		chain.doFilter(request, response);
	}

	private void setTokenCookie(HttpServletResponse response, Token token) {
		CookieUtils.setCookie(response, tokenFactory.createAccessTokenCookie(token));
		CookieUtils.setCookie(response, tokenFactory.createRefreshTokenCookie(token));
	}

	private void setAuthentication(String accessToken) {
		MemberAuthentication memberAuthentication = tokenService.parseMemberAuthenticationToken(accessToken);
		Authentication auth = getAuthentication(memberAuthentication);
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	private Authentication getAuthentication(MemberAuthentication memberAuthentication) {
		return new UsernamePasswordAuthenticationToken(
			memberAuthentication,
			Strings.EMPTY,
			memberAuthentication.getSimpleGrantedAuthority()
		);
	}
}
