package co.fineants.api.infra.mail;

import java.util.Map;

import org.springframework.mail.javamail.JavaMailSender;

import co.fineants.api.domain.member.service.MailHtmlRender;
import jakarta.mail.internet.MimeMessage;

public class VerifyCodeMimeMessageFactory implements MimeMessageFactory {

	private final MailHtmlRender htmlRender;
	private final JavaMailSender mailSender;
	private final String subject;

	public VerifyCodeMimeMessageFactory(MailHtmlRender htmlRender, JavaMailSender mailSender, String subject) {
		this.htmlRender = htmlRender;
		this.mailSender = mailSender;
		this.subject = subject;
	}

	@Override
	public MimeMessage create(String to, Map<String, Object> variables) {
		String html = htmlRender.render(variables);
		MimeMessage message = mailSender.createMimeMessage();
		return MimeMessageBuilder.builder(message, to, subject)
			.html(html)
			.build();
	}
}
