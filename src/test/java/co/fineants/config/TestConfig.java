package co.fineants.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

import com.google.firebase.messaging.FirebaseMessaging;

import co.fineants.api.domain.dividend.domain.calculator.ExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.calculator.FileExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.reader.HolidayFileReader;
import co.fineants.api.domain.exchangerate.client.ExchangeRateClient;
import co.fineants.api.domain.fcm.service.FirebaseMessagingService;
import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.repository.FileHolidayRepository;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.api.domain.portfolio.service.PortfolioCacheService;
import co.fineants.api.global.common.delay.DelayManager;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.infra.mail.EmailService;

@TestConfiguration
public class TestConfig {
	@MockBean
	private EmailService emailService;

	@SpyBean
	private LocalDateTimeService spyLocalDateTimeService;

	@MockBean
	private FirebaseMessaging firebaseMessaging;

	@MockBean
	private KisService kisService;

	@MockBean
	private KisClient kisClient;

	@SpyBean
	private DelayManager delayManager;

	@SpyBean
	private JavaMailSender javaMailSender;

	@MockBean
	private FirebaseMessagingService firebaseMessagingService;

	@SpyBean
	private PortfolioCacheService portfolioCacheService;

	@Bean
	public ExDividendDateCalculator exDividendDateCalculator() {
		return new FileExDividendDateCalculator(new FileHolidayRepository(new HolidayFileReader()));
	}

	@Bean
	public ExchangeRateClient mockedExchangeRateClient() {
		return Mockito.mock(ExchangeRateClient.class);
	}
}
