package co.fineants.member.application;

import static org.assertj.core.api.Assertions.*;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.global.errors.exception.business.PasswordAuthenticationException;
import co.fineants.member.application.SignupValidatorService;

class SignupValidatorServiceTest extends AbstractContainerBaseTest {
	@Autowired
	private SignupValidatorService service;

	@DisplayName("사용자는 비밀번호와 비밀번호 확인이 일치하지 않아 회원가입 할 수 없다")
	@Test
	void signup_whenNotMatchPasswordAndPasswordConfirm_thenResponse400Error() {
		// given
		String password = "password123!";
		String passwordConfirm = "password1234!";

		// when
		Throwable throwable = catchThrowable(() -> service.validatePassword(password, passwordConfirm));

		// then
		assertThat(throwable)
			.isInstanceOf(PasswordAuthenticationException.class)
			.hasMessage(Strings.EMPTY);
	}

}
