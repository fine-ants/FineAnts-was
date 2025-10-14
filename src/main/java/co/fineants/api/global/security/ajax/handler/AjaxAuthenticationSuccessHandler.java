package co.fineants.api.global.security.ajax.handler;

import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.fineants.api.domain.member.repository.RoleRepository;
import co.fineants.api.domain.role.domain.Role;
import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.security.factory.TokenFactory;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import co.fineants.api.global.security.oauth.dto.Token;
import co.fineants.api.global.security.oauth.service.TokenService;
import co.fineants.api.global.success.MemberSuccessCode;
import co.fineants.api.global.util.CookieUtils;
import co.fineants.member.domain.Member;
import co.fineants.member.presentation.dto.response.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class AjaxAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
	private final ObjectMapper objectMapper;
	private final TokenService tokenService;
	private final TokenFactory tokenFactory;
	private final RoleRepository roleRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		Member member = (Member)authentication.getPrincipal();

		response.setStatus(HttpStatus.OK.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("utf-8");

		Set<String> roleNames = roleRepository.findAllById(member.getRoleIds()).stream()
			.map(Role::getRoleName)
			.collect(Collectors.toSet());
		Token token = tokenService.generateToken(MemberAuthentication.from(member, roleNames), new Date());
		ApiResponse<LoginResponse> body = ApiResponse.success(MemberSuccessCode.OK_LOGIN);

		CookieUtils.setCookie(response, tokenFactory.createAccessTokenCookie(token));
		CookieUtils.setCookie(response, tokenFactory.createRefreshTokenCookie(token));

		objectMapper.writeValue(response.getWriter(), body);
		log.info("Member {} has successfully logged", member.getNickname());
	}
}
