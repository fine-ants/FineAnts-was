package co.fineants.config;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import co.fineants.api.infra.mail.EmailService;
import co.fineants.api.infra.s3.service.AmazonS3Service;

@TestConfiguration
public class MemberServiceTestConfig {

	@Autowired
	@Qualifier("defaultAmazonS3Service")
	private AmazonS3Service defaultAmazonS3Service;

	@Bean
	public EmailService mockEmailService() {
		return Mockito.mock(EmailService.class);
	}

	@Bean
	public AmazonS3Service mockAmazonS3Service() {
		return Mockito.mock(AmazonS3Service.class);
	}

	@Bean
	public AmazonS3Service defaultAmazonS3Service() {
		return defaultAmazonS3Service;
	}
}
