package co.fineants.api.domain.member.domain.factory;

import java.nio.charset.StandardCharsets;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import co.fineants.api.global.errors.exception.business.MailInvalidInputException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

public class MimeMessageFactory {

	private final JavaMailSender mailSender;

	public MimeMessageFactory(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public MimeMessage create(String to, String subject, String html) {
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
