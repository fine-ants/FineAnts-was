package co.fineants.api.infra.mail;

import static org.mockito.BDDMockito.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring6.SpringTemplateEngine;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.exchangerate.domain.dto.response.ExchangeRateFetchResponse;
import jakarta.mail.internet.MimeMessage;

class JavaEmailServiceTest extends AbstractContainerBaseTest {

	private EmailService service;

	@Autowired
	private JavaMailSender spyJavaMailSender;

	@Autowired
	private SpringTemplateEngine springTemplateEngine;

	@Value("${admin.email}")
	private String adminEmail;

	@BeforeEach
	void setUp() {
		service = new JavaEmailService(spyJavaMailSender, springTemplateEngine, adminEmail);
	}

	@DisplayName("서버는 이메일을 전송한다")
	@Test
	void sendEmail() {
		// given
		willDoNothing().given(spyJavaMailSender).send(any(MimeMessage.class));

		String verifyCode = "123456";
		String to = "dragonbead95@naver.com";
		String subject = "Finants 회원가입 인증 코드";
		String templateName = "mail-templates/verify-email_template";
		Map<String, String> values = Map.of("verifyCode", verifyCode);
		// when
		service.sendEmail(to, subject, templateName, values);

		// then
		verify(spyJavaMailSender, times(1)).send(any(MimeMessage.class));
	}

	@DisplayName("관리자에게 환율 API 서버 오류 메일을 전송한다")
	@Test
	void sendExchangeRateErrorEmail() {
		// given
		willDoNothing().given(spyJavaMailSender).send(any(MimeMessage.class));
		String errorMessage = ExchangeRateFetchResponse.requestExceeded().toException().getErrorCodeMessage();
		// when
		service.sendExchangeRateErrorEmail(errorMessage);
		// then
		verify(spyJavaMailSender, times(1)).send(any(MimeMessage.class));
	}
}
