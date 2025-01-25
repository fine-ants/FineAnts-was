package co.fineants.api.infra.mail;

public class FakeEmailService implements EmailService {
	@Override
	public void sendEmail(String to, String subject, String body) {
		// this service not send the email
	}
}
