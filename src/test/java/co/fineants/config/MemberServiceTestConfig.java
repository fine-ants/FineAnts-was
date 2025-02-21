package co.fineants.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;

import co.fineants.api.infra.mail.EmailService;
import co.fineants.api.infra.s3.service.AmazonS3Service;

@TestConfiguration
public class MemberServiceTestConfig {

	public EmailService mockEmailService() {
		return Mockito.mock(EmailService.class);
	}

	public AmazonS3Service mockAmazonS3Service() {
		return Mockito.mock(AmazonS3Service.class);
	}
}
