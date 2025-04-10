package co.fineants.api.infra.mail;

import java.nio.charset.StandardCharsets;
import java.util.Map;

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

	public JavaEmailService(JavaMailSender mailSender, SpringTemplateEngine springTemplateEngine) {
		this.mailSender = mailSender;
		this.springTemplateEngine = springTemplateEngine;
	}

	@Override
	public void sendEmail(String to, String subject, String templateName, Map<String, String> values) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
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
}
