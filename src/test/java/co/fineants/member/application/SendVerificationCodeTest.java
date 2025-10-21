package co.fineants.member.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.internal.verification.Times;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.infra.mail.EmailService;
import jakarta.mail.internet.MimeMessage;

class SendVerificationCodeTest extends AbstractContainerBaseTest {

	@Autowired
	private SendVerificationCode service;

	@Autowired
	private EmailService mockEmailService;

	@DisplayName("이메일로 회원가입 검증 코드 전송 테스트")
	@Test
	void send() {
		// given
		String email = "dragonbead95@naver.com";
		// when
		service.send(email);
		// then
		BDDMockito.then(mockEmailService)
			.should(new Times(1))
			.sendEmail(ArgumentMatchers.any(MimeMessage.class));
	}
}
