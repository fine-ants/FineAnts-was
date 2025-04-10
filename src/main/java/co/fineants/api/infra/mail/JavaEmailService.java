package co.fineants.api.infra.mail;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JavaEmailService implements EmailService {
	private final JavaMailSender mailSender;
	private final SpringTemplateEngine springTemplateEngine;
	private final String adminEmail;

	public JavaEmailService(JavaMailSender mailSender, SpringTemplateEngine springTemplateEngine,
		@Value("${admin.email}") String adminEmail) {
		this.mailSender = mailSender;
		this.springTemplateEngine = springTemplateEngine;
		this.adminEmail = adminEmail;
	}

	@Override
	public void sendEmail(String to, String subject, String body, String templateName, Map<String, String> values) {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper;
		try {
			helper = new MimeMessageHelper(message, true, "UTF-8");
			// 수신자 설정
			helper.setTo(to);
			// 메일 제목 설정
			helper.setSubject(subject);
			// 템플릿에 전달할 데이터 설정
			Context context = new Context();
			values.forEach(context::setVariable);
			// 메일 내용 설정: 템플릿 프로세스
			String html = springTemplateEngine.process(templateName, context);
			helper.setText(html, true);
			// 메일 전송
			mailSender.send(message);
		} catch (MessagingException e) {
			log.error("Failed to create MimeMessageHelper", e);
			throw new IllegalArgumentException("Failed to create MimeMessageHelper", e);
		}
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
}
