package co.fineants.member.application;

import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

import co.fineants.api.global.security.factory.TokenFactory;
import co.fineants.api.global.security.oauth.dto.Token;
import co.fineants.api.global.util.CookieUtils;
import co.fineants.member.domain.JwtRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogoutMember {

	private final JwtRepository jwtRepository;
	private final TokenFactory tokenFactory;

	public void logout(HttpServletRequest request, HttpServletResponse response) {
		// clear authentication
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
		securityContextLogoutHandler.logout(request, response, authentication);

		// ban accessToken
		String accessToken = CookieUtils.getAccessToken(request);
		jwtRepository.banAccessToken(accessToken);

		// ban refreshToken
		String refreshToken = CookieUtils.getRefreshToken(request);
		jwtRepository.banRefreshToken(refreshToken);

		setExpiredCookies(response);
	}

	private void setExpiredCookies(HttpServletResponse response) {
		ResponseCookie expiredAccessTokenCookie = tokenFactory.createExpiredAccessTokenCookie(Token.empty());
		CookieUtils.setCookie(response, expiredAccessTokenCookie);
		ResponseCookie expiredRefreshTokenCookie = tokenFactory.createExpiredRefreshTokenCookie(Token.empty());
		CookieUtils.setCookie(response, expiredRefreshTokenCookie);
	}
}
