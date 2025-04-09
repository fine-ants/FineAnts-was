package co.fineants.api.infra.mail;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
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
	public void sendExchangeRateErrorEmail(String errorMessage) {
		EmailTemplateProcessor processor = new EmailTemplateProcessor();
		String path = "email/exchange-rate-fail-notification_template.txt";
		String apiUrl = "https://exchange-rate-api1.p.rapidapi.com";
		Map<String, String> placeholders = Map.of(
			"failedAt", LocalDateTime.now().toString(),
			"apiUrl", apiUrl,
			"errorMessage", errorMessage
		);
		EmailTemplate emailTemplate = processor.processTemplate(path, placeholders);

		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(adminEmail);
		message.setSubject(emailTemplate.getSubject());
		message.setText(emailTemplate.getBody());
		try {
			mailSender.send(message);
		} catch (MailException e) {
			log.warn("환율 API 서버 오류 메일 발송 실패, message=" + message, e);
			throw new IllegalArgumentException("Failed to send email", e);
		}
	}

	private EmailTemplate loadTemplate(String path) throws IllegalArgumentException {
		ClassPathResource resource = new ClassPathResource(path);
		String content;
		try {
			content = new String(resource.getInputStream().readAllBytes());
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to load email template: " + path, e);
		}
		String[] parts = content.split("---BODY---");
		String subject = parts[0].replace("---SUBJECT---", "").trim();
		String body = parts.length > 1 ? parts[1].trim() : "";
		return new EmailTemplate(subject, body);
	}
}
