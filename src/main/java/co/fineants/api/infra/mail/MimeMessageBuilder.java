package co.fineants.api.infra.mail;

import java.nio.charset.StandardCharsets;

import org.springframework.mail.javamail.MimeMessageHelper;

import co.fineants.api.global.errors.exception.business.MailInvalidInputException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

public class MimeMessageBuilder {
	private final MimeMessage message;
	private final String to;
	private final String subject;
	private String html;

	private MimeMessageBuilder(MimeMessage message, String to, String subject) {
		this.message = message;
		this.to = to;
		this.subject = subject;
	}

	public static MimeMessageBuilder builder(MimeMessage message, String to, String subject) {
		return new MimeMessageBuilder(message, to, subject);
	}

	public MimeMessageBuilder html(String html) {
		this.html = html;
		return this;
	}

	public MimeMessage build() {
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(html, true);
		} catch (MessagingException e) {
			String value = String.format("to=%s, subject=%s", to, subject);
			throw new MailInvalidInputException(value, e);
		}
		return message;
	}
}
