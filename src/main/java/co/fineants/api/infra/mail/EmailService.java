package co.fineants.api.infra.mail;

public interface EmailService {
	void sendEmail(String to, String subject, String body);

	void sendExchangeRateErrorEmail();
}
