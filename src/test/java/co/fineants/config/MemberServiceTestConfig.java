package co.fineants.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import co.fineants.api.infra.mail.EmailService;
import co.fineants.api.infra.mail.FakeEmailService;

@TestConfiguration
public class MemberServiceTestConfig {
	@Bean
	public EmailService emailService() {
		return new FakeEmailService();
	}
}
