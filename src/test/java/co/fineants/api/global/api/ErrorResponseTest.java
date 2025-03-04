package co.fineants.api.global.api;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.api.global.errors.errorcode.MemberErrorCode;
import co.fineants.api.global.errors.exception.BadRequestException;
import co.fineants.api.global.errors.exception.FineAntsException;

class ErrorResponseTest {

	@DisplayName("회원 조회 실패 시 예외 응답이 올바르게 생성되는지 검증한다.")
	@Test
	void shouldGenerateCorrectErrorResponse_whenMemberNotFound() {
		// given
		FineAntsException exception = new BadRequestException(MemberErrorCode.NOT_FOUND_MEMBER);
		ErrorResponse errorResponse = new ErrorResponse(exception);
		// when
		String actual = errorResponse.toString();
		// then
		String expected = "ErrorResponse(code=404, status=Not Found, message=회원을 찾지 못하였습니다.)";
		Assertions.assertThat(actual).isEqualTo(expected);
	}
}
