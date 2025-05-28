package co.fineants.api.infra.mail;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
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
	public void sendEmail(MimeMessage message) {
		mailSender.send(message);
	}

	@Override
	public void sendEmail(String to, String subject, String templateName, Map<String, Object> variables) {
		MimeMessage message;
		try {
			message = createMimeMessage(to, subject, templateName, variables);
		} catch (MessagingException e) {
			throw new IllegalArgumentException("Failed to create MimeMessageHelper", e);
		}
		// 메일 전송
		mailSender.send(message);
	}

	@NotNull
	private MimeMessage createMimeMessage(String to, String subject, String templateName,
		Map<String, Object> variables) throws MessagingException {
		// 템플릿에 전달할 데이터 설정
		Context context = new Context(Locale.KOREA, variables);
		return mimeMessageBuilder(to, subject)
			.html(springTemplateEngine.process(templateName, context)) // 메일 내용 설정: 템플릿 프로세스
			.build();
	}

	private MimeMessageBuilder mimeMessageBuilder(String to, String subject) {
		return new MimeMessageBuilder(to, subject);
	}

	private class MimeMessageBuilder {
		private final String to;
		private final String subject;
		private String html;

		public MimeMessageBuilder(String to, String subject) {
			this.to = to;
			this.subject = subject;
		}

		public MimeMessageBuilder html(String html) {
			this.html = html;
			return this;
		}

		public MimeMessage build() throws MessagingException {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(html, true);
			return message;
		}
	}
}
