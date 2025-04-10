package co.fineants.api.infra.mail;

import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

	@BeforeEach
	void setUp() {
		service = new JavaEmailService(spyJavaMailSender, springTemplateEngine);
	}

	@DisplayName("서버는 이메일을 전송한다")
	@Test
	void sendEmail() {
		// given
		willDoNothing().given(spyJavaMailSender).send(any(MimeMessage.class));

		String verifyCode = "123456";
		String to = "user1@gmail.com";
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
	void sendExchangeRateErrorNotification() {
		// given
		willDoNothing().given(spyJavaMailSender).send(any(MimeMessage.class));
		String to = "user1@gmail.com";
		String subject = "환율 API 서버 오류";
		String templateName = "mail-templates/exchange-rate-fail-notification_template";
		String apiUrl = "https://exchange-rate-api1.p.rapidapi.com";
		String errorMessage = ExchangeRateFetchResponse.requestExceeded().toException().getErrorCodeMessage();
		Map<String, String> values = Map.of(
			"failedAt", LocalDateTime.now().toString(),
			"apiUrl", apiUrl,
			"errorMessage", errorMessage
		);
		// when
		service.sendEmail(to, subject, templateName, values);
		// then
		verify(spyJavaMailSender, times(1)).send(any(MimeMessage.class));
	}
}
