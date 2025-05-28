package co.fineants.api.infra.mail;

import java.util.Map;

import jakarta.mail.internet.MimeMessage;

public interface EmailService {

	void sendEmail(String to, String subject, String templateName, Map<String, Object> variables);

	void sendEmail(MimeMessage message);
}
