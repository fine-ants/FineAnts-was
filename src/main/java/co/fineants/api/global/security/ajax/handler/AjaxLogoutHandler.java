package co.fineants.api.global.security.ajax.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import co.fineants.member.application.LogoutMember;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AjaxLogoutHandler implements LogoutHandler {
	private final LogoutMember logoutMember;

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		logoutMember.logout(request, response);
	}
}
