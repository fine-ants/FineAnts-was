package co.fineants.api.infra.mail;

import jakarta.mail.internet.MimeMessage;

public interface EmailService {

	void sendEmail(MimeMessage message);
}
