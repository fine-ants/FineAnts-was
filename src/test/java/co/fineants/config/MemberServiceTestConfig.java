package co.fineants.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import co.fineants.api.infra.mail.EmailService;
import co.fineants.api.infra.mail.FakeEmailService;
import co.fineants.api.infra.s3.service.AmazonS3Service;
import co.fineants.api.infra.s3.service.FakeAmazonS3Service;

@TestConfiguration
public class MemberServiceTestConfig {
	@Bean
	public EmailService emailService() {
		return new FakeEmailService();
	}

	@Bean
	public AmazonS3Service amazonS3Service() {
		return new FakeAmazonS3Service();
	}
}
