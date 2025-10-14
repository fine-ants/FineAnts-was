package co.fineants.api.infra.mail;

import java.util.Map;

import jakarta.mail.internet.MimeMessage;

public interface MimeMessageFactory {
	MimeMessage create(String to, Map<String, Object> variables);
}
