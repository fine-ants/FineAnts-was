package co.fineants.api.infra.mail;

import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import co.fineants.AbstractContainerBaseTest;

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

		String to = "dragonbead95@naver.com";
		String subject = "스프링부트 메일 테스트";
		String body = "스프링부트 메일 테스트 내용입니다.";

		// when
		service.sendEmail(to, subject, body);

		// then
		verify(mockedJavaMailSender, times(1)).send(any(SimpleMailMessage.class));
	}

	@DisplayName("관리자에게 환율 API 서버 오류 메일을 전송한다")
	@Test
	void sendExchangeRateErrorEmail() {
		// given
		willDoNothing().given(mockedJavaMailSender).send(any(SimpleMailMessage.class));
		// when
		service.sendExchangeRateErrorEmail();
		// then
		verify(mockedJavaMailSender, times(1)).send(any(SimpleMailMessage.class));
	}
}
