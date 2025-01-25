package co.fineants.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import co.fineants.api.infra.mail.EmailService;
import co.fineants.api.infra.s3.service.AmazonS3Service;
import lombok.RequiredArgsConstructor;

@TestConfiguration
@RequiredArgsConstructor
public class MemberServiceTestConfig {

	@Bean
	public EmailService emailService() {
		return Mockito.mock(EmailService.class);
	}

	@Bean
	@Primary
	public AmazonS3Service mockAmazonS3Service() {
		return Mockito.mock(AmazonS3Service.class);
	}
}
