package co.fineants.member.application;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.global.errors.exception.business.VerifyCodeInvalidInputException;
import co.fineants.member.domain.VerifyCodeRepository;

class VerifyCodeTest extends AbstractContainerBaseTest {

	@Autowired
	private VerifyCodeRepository repository;

	@Autowired
	private VerifyCode verifyCode;

	@DisplayName("사용자는 검증코드를 제출하여 검증코드가 일치하는지 검사한다")
	@Test
	void checkVerifyCode() {
		// given
		String email = "dragonbead95@naver.com";
		String code = "123456";
		repository.save(email, code);
		// when & then
		Assertions.assertDoesNotThrow(() -> verifyCode.verifyCode(email, code));
	}

	@DisplayName("사용자는 매치되지 않은 검증 코드를 전달하며 검사를 요청했을때 예외가 발생한다")
	@Test
	void checkVerifyCode_whenNotMatchVerifyCode_thenThrowException() {
		// given
		String email = "dragonbead95@naver.com";
		String code = "234567";

		// when
		Throwable throwable = catchThrowable(() -> verifyCode.verifyCode(email, code));

		// then
		assertThat(throwable)
			.isInstanceOf(VerifyCodeInvalidInputException.class)
			.hasMessage("234567");
	}

}
