package co.fineants.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

import com.google.firebase.messaging.FirebaseMessaging;

import co.fineants.api.domain.dividend.domain.calculator.ExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.calculator.FileExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.reader.HolidayFileReader;
import co.fineants.api.domain.exchangerate.client.ExchangeRateWebClient;
import co.fineants.api.domain.fcm.service.FirebaseMessagingService;
import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.repository.FileHolidayRepository;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.api.domain.member.service.VerifyCodeGenerator;
import co.fineants.api.domain.member.service.VerifyCodeManagementService;
import co.fineants.api.global.common.delay.DelayManager;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.infra.mail.EmailService;
import co.fineants.api.infra.s3.service.AmazonS3Service;

@TestConfiguration
public class TestConfig {
	@MockBean
	private AmazonS3Service mockedAmazonS3Service;

	@MockBean
	private VerifyCodeManagementService verifyCodeManagementService;

	@MockBean
	private VerifyCodeGenerator verifyCodeGenerator;

	@MockBean
	private EmailService emailService;

	@MockBean
	private ExchangeRateWebClient mockedExchangeRateWebClient;

	@SpyBean
	private LocalDateTimeService mockedLocalDateTimeService;

	@MockBean
	private FirebaseMessaging firebaseMessaging;

	@MockBean
	private KisService kisService;

	@MockBean
	private KisClient kisClient;

	@SpyBean
	private DelayManager delayManager;

	@MockBean
	private JavaMailSender javaMailSender;

	@MockBean
	private FirebaseMessagingService firebaseMessagingService;

	@Bean
	public ExDividendDateCalculator exDividendDateCalculator() {
		return new FileExDividendDateCalculator(new FileHolidayRepository(new HolidayFileReader()));
	}
}
