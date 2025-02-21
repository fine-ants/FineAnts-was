package co.fineants.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;

import co.fineants.api.domain.kis.service.KisService;
import co.fineants.api.domain.member.service.TokenManagementService;
import co.fineants.api.domain.member.service.VerifyCodeGenerator;
import co.fineants.api.domain.member.service.VerifyCodeManagementService;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.infra.mail.EmailService;
import co.fineants.api.infra.s3.service.AmazonS3Service;

@TestConfiguration
public class MockFactory {

	public EmailService mockEmailService() {
		return Mockito.mock(EmailService.class);
	}

	public AmazonS3Service mockAmazonS3Service() {
		return Mockito.mock(AmazonS3Service.class);
	}

	public TokenManagementService mockTokenManagementService() {
		return Mockito.mock(TokenManagementService.class);
	}

	public VerifyCodeGenerator mockVerifyCodeGenerator() {
		return Mockito.mock(VerifyCodeGenerator.class);
	}

	public VerifyCodeManagementService mockVerifyCodeManagementService() {
		return Mockito.mock(VerifyCodeManagementService.class);
	}

	public LocalDateTimeService mockLocalDateTimeService() {
		return Mockito.mock(LocalDateTimeService.class);
	}

	public KisService mockeKisService() {
		return Mockito.mock(KisService.class);
	}
}
