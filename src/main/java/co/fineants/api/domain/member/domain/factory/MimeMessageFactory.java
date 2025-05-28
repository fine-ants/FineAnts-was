package co.fineants.api.domain.member.domain.factory;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import co.fineants.api.global.errors.exception.business.MailInvalidInputException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

public class MimeMessageFactory {

	private final JavaMailSender mailSender;
	private final SpringTemplateEngine springTemplateEngine;

	public MimeMessageFactory(JavaMailSender mailSender, SpringTemplateEngine springTemplateEngine) {
		this.mailSender = mailSender;
		this.springTemplateEngine = springTemplateEngine;
	}

	public MimeMessage create(String to, String subject, String html) {
		return mimeMessageBuilder(to, subject)
			.html(html)
			.build();
	}

	public MimeMessage create(String to, String subject, String templateName,
		Map<String, Object> variables) {
		// 템플릿에 전달할 데이터 설정
		Context context = new Context(Locale.KOREA, variables);
		// 메일 내용 설정: 템플릿 프로세스
		String html = springTemplateEngine.process(templateName, context);
		return mimeMessageBuilder(to, subject)
			.html(html)
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

		public MimeMessage build() {
			MimeMessage message = mailSender.createMimeMessage();

			try {
				MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
				helper.setTo(to);
				helper.setSubject(subject);
				helper.setText(html, true);
			} catch (MessagingException e) {
				String value = "to=%s, subject=%s, html=%s".formatted(to, subject, html);
				throw new MailInvalidInputException(value, e);
			}
			return message;
		}
	}
}
