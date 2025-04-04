package co.fineants.api.global.security.ajax.handler;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.errors.errorcode.CustomErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class AjaxAuthenticationFailHandler implements AuthenticationFailureHandler {

	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException exception) throws IOException {
		CustomErrorCode errorCode = CustomErrorCode.LOGIN_FAIL;
		HttpStatus badRequest = HttpStatus.BAD_REQUEST;
		ApiResponse<String> body = ApiResponse.error(badRequest, errorCode);
		response.setStatus(badRequest.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("utf-8");
		response.getWriter().write(objectMapper.writeValueAsString(body));
	}
}
