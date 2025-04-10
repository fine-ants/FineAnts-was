package co.fineants.api.infra.mail;

import static org.mockito.BDDMockito.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.exchangerate.domain.dto.response.ExchangeRateFetchResponse;

class JavaEmailServiceTest extends AbstractContainerBaseTest {

	private EmailService service;

	@Autowired
	private JavaMailSender mockedJavaMailSender;

	@Value("${admin.email}")
	private String adminEmail;

	@BeforeEach
	void setUp() {
		service = new JavaEmailService(mockedJavaMailSender, adminEmail);
	}

	@DisplayName("서버는 이메일을 전송한다")
	@Test
	void sendEmail() {
		// given
		willDoNothing().given(mockedJavaMailSender).send(any(SimpleMailMessage.class));

		String verifyCode = "123456";
		String to = "dragonbead95@naver.com";
		String subject = "Finants 회원가입 인증 코드";
		String body = String.format("인증코드를 회원가입 페이지에 입력해주세요: %s", verifyCode);
		String templateName = "email/verify-email_template.txt";
		Map<String, String> values = Map.of("verifyCode", verifyCode);
		// when
		service.sendEmail(to, subject, body, templateName, values);

		// then
		verify(mockedJavaMailSender, times(1)).send(any(SimpleMailMessage.class));
	}

	@DisplayName("관리자에게 환율 API 서버 오류 메일을 전송한다")
	@Test
	void sendExchangeRateErrorEmail() {
		// given
		willDoNothing().given(mockedJavaMailSender).send(any(SimpleMailMessage.class));
		String errorMessage = ExchangeRateFetchResponse.requestExceeded().toException().getErrorCodeMessage();
		// when
		service.sendExchangeRateErrorEmail(errorMessage);
		// then
		verify(mockedJavaMailSender, times(1)).send(any(SimpleMailMessage.class));
	}
}
