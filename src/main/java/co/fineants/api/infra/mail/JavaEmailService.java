package co.fineants.api.infra.mail;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JavaEmailService implements EmailService {
	private final JavaMailSender mailSender;

	public JavaEmailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	@Override
	public void sendEmail(MimeMessage message) {
		mailSender.send(message);
	}

}
