package co.fineants.api.infra.mail;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import co.fineants.api.global.errors.exception.email.EmailSendException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JavaEmailService implements EmailService {
	private final JavaMailSender mailSender;

	@Override
	public void sendEmail(String to, String subject, String body) throws EmailSendException {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setSubject(subject);
		message.setText(body);
		try {
			mailSender.send(message);
		} catch (MailException exception) {
			String errorMessage = "can't send the email";
			throw new EmailSendException(errorMessage, exception);
		}
	}
}
