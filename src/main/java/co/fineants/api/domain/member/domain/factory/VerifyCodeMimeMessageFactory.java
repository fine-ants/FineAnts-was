package co.fineants.api.domain.member.domain.factory;

import java.util.Map;

import org.springframework.mail.javamail.JavaMailSender;

import co.fineants.api.domain.member.service.MailHtmlRender;
import jakarta.mail.internet.MimeMessage;

public class VerifyCodeMimeMessageFactory implements MimeMessageFactory {

	private MailHtmlRender htmlRender;
	private JavaMailSender mailSender;
	private String subject;

	public VerifyCodeMimeMessageFactory(MailHtmlRender htmlRender, JavaMailSender mailSender, String subject) {
		this.htmlRender = htmlRender;
		this.mailSender = mailSender;
		this.subject = subject;
	}

	@Override
	public MimeMessage create(String to, String verifyCode) {
		Map<String, Object> variables = Map.of("verifyCode", verifyCode);
		String html = htmlRender.render(variables);
		MimeMessage message = mailSender.createMimeMessage();
		return MimeMessageBuilder.builder(message, to, subject)
			.html(html)
			.build();
	}
}
