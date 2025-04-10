package co.fineants.api.infra.mail;

import java.util.Map;

public interface EmailService {

	void sendEmail(String to, String subject, String templateName, Map<String, String> values);

	void sendExchangeRateErrorEmail(String errorMessage);
}
