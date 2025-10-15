package co.fineants.member.application;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.internal.verification.Times;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.global.errors.exception.business.VerifyCodeInvalidInputException;
import co.fineants.api.infra.mail.EmailService;
import co.fineants.member.application.SignupVerificationService;
import co.fineants.member.domain.VerifyCodeRepository;
import jakarta.mail.internet.MimeMessage;

class SignupVerificationServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private SignupVerificationService service;

	@Autowired
	private EmailService mockEmailService;

	@Autowired
	private VerifyCodeRepository verifyCodeRedisRepository;

	@DisplayName("이메일로 회원가입 검증 코드 전송 테스트")
	@Test
	void givenEmail_whenSendSignupVerification_thenShouldOnceSendEmail() {
		// given
		String email = "dragonbead95@naver.com";
		// when
		service.sendSignupVerification(email);
		// then
		BDDMockito.then(mockEmailService)
			.should(new Times(1))
			.sendEmail(ArgumentMatchers.any(MimeMessage.class));
	}

	@DisplayName("사용자는 검증코드를 제출하여 검증코드가 일치하는지 검사한다")
	@Test
	void checkVerifyCode() {
		// given
		String email = "dragonbead95@naver.com";
		String code = "123456";
		verifyCodeRedisRepository.save(email, code);
		// when & then
		Assertions.assertDoesNotThrow(() -> service.verifyCode(email, code));
	}

	@DisplayName("사용자는 매치되지 않은 검증 코드를 전달하며 검사를 요청했을때 예외가 발생한다")
	@Test
	void checkVerifyCode_whenNotMatchVerifyCode_thenThrowException() {
		// given
		String email = "dragonbead95@naver.com";
		String code = "234567";

		// when
		Throwable throwable = catchThrowable(() -> service.verifyCode(email, code));

		// then
		assertThat(throwable)
			.isInstanceOf(VerifyCodeInvalidInputException.class)
			.hasMessage("234567");
	}

}
