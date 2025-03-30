package co.fineants.api.global.errors.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.errors.exception.FineAntsException;
import co.fineants.api.global.errors.exception.temp.AuthenticationException;
import co.fineants.api.global.errors.exception.temp.AuthorizationException;
import co.fineants.api.global.errors.exception.temp.BusinessException;
import co.fineants.api.global.errors.exception.temp.DuplicateException;
import co.fineants.api.global.errors.exception.temp.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(FineAntsException.class)
	public ResponseEntity<ApiResponse<Object>> handleFineANtsException(FineAntsException exception) {
		ApiResponse<Object> body = ApiResponse.error(exception.getErrorCode());
		return ResponseEntity.status(exception.getErrorCode().getHttpStatus()).body(body);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException exception) {
		List<Map<String, String>> data = exception.getBindingResult().getFieldErrors().stream()
			.map(error -> {
				Map<String, String> errors = new HashMap<>();
				errors.put("field", error.getField());
				errors.put("defaultMessage", error.getDefaultMessage());
				return errors;
			}).toList();
		ApiResponse<Object> body = ApiResponse.of(
			HttpStatus.BAD_REQUEST,
			"잘못된 입력형식입니다",
			data
		);
		return ResponseEntity.badRequest().body(body);
	}

	@ExceptionHandler(MissingServletRequestPartException.class)
	public ResponseEntity<ApiResponse<Object>> handleMissingServletRequestPartException(
		MissingServletRequestPartException exception) {
		ApiResponse<Object> body = ApiResponse.of(HttpStatus.BAD_REQUEST, exception.getMessage(), null);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Object>> handleException(Exception exception) {
		ApiResponse<Object> body = ApiResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(),
			exception.toString());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException exception) {
		HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		String message = exception.getErrorCode().getMessage();
		String data = null;
		if (exception instanceof DuplicateException duplicateException) {
			httpStatus = HttpStatus.CONFLICT;
			data = duplicateException.getValue();
		} else if (exception instanceof AuthenticationException authenticationException) {
			httpStatus = HttpStatus.UNAUTHORIZED;
			data = authenticationException.getValue();
		} else if (exception instanceof AuthorizationException authorizationException) {
			httpStatus = HttpStatus.FORBIDDEN;
			data = authorizationException.getValue();
		} else if (exception instanceof NotFoundException notFoundException) {
			httpStatus = HttpStatus.NOT_FOUND;
			data = notFoundException.getValue();
		}
		ApiResponse<Object> body = ApiResponse.error(httpStatus, message, data);
		return ResponseEntity.status(httpStatus).body(body);
	}
}
