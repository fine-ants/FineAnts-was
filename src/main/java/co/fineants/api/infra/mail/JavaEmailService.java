package co.fineants.api.infra.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JavaEmailService implements EmailService {
	private final JavaMailSender mailSender;
	private final String adminEmail;

	public JavaEmailService(JavaMailSender mailSender, @Value("${admin.email}") String adminEmail) {
		this.mailSender = mailSender;
		this.adminEmail = adminEmail;
	}

	@Override
	public void sendEmail(String to, String subject, String body) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setSubject(subject);
		message.setText(body);
		mailSender.send(message);
	}

	/**
	 * 환율 API 서버로부터 환율 정보를 가져오지 못했을 때 발송하는 메일
	 */
	@Override
	public void sendExchangeRateErrorEmail() {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(adminEmail);
		message.setSubject("환율 API 서버 오류");
		message.setText("환율 API 서버로부터 환율 정보를 가져오지 못했습니다. 서버 상태를 확인해주세요.");
		try {
			mailSender.send(message);
		} catch (MailException e) {
			log.warn("환율 API 서버 오류 메일 발송 실패, adminEmail=" + adminEmail, e);
		}
	}
}
