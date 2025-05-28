package co.fineants.api.domain.member.domain.factory;

import jakarta.mail.internet.MimeMessage;

public interface MimeMessageFactory {
	MimeMessage create(String to, String verifyCode);
}
