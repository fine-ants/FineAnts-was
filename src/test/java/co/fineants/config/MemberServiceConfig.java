package co.fineants.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.fineants.api.domain.fcm.repository.FcmRepository;
import co.fineants.api.domain.gainhistory.repository.PortfolioGainHistoryRepository;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.member.repository.RoleRepository;
import co.fineants.api.domain.member.service.MemberService;
import co.fineants.api.domain.member.service.TokenManagementService;
import co.fineants.api.domain.member.service.VerifyCodeGenerator;
import co.fineants.api.domain.member.service.VerifyCodeManagementService;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.domain.notificationpreference.repository.NotificationPreferenceRepository;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.domain.stock_target_price.repository.StockTargetPriceRepository;
import co.fineants.api.domain.stock_target_price.repository.TargetPriceNotificationRepository;
import co.fineants.api.domain.watchlist.repository.WatchListRepository;
import co.fineants.api.domain.watchlist.repository.WatchStockRepository;
import co.fineants.api.global.security.factory.TokenFactory;
import co.fineants.api.infra.mail.EmailService;
import co.fineants.api.infra.s3.service.AmazonS3Service;
import lombok.RequiredArgsConstructor;

@TestConfiguration
@RequiredArgsConstructor
public class MemberServiceConfig {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final WatchListRepository watchListRepository;
	private final WatchStockRepository watchStockRepository;
	private final PortfolioHoldingRepository portfolioHoldingRepository;
	private final PortfolioRepository portfolioRepository;
	private final PortfolioGainHistoryRepository portfolioGainHistoryRepository;
	private final PurchaseHistoryRepository purchaseHistoryRepository;
	private final NotificationPreferenceRepository notificationPreferenceRepository;
	private final NotificationRepository notificationRepository;
	private final FcmRepository fcmRepository;
	private final StockTargetPriceRepository stockTargetPriceRepository;
	private final TargetPriceNotificationRepository targetPriceNotificationRepository;
	private final TokenManagementService tokenManagementService;
	private final RoleRepository roleRepository;
	private final TokenFactory tokenFactory;

	@Bean
	public MemberService memberService() {
		EmailService mockedEmailService = mockedEmailService();
		AmazonS3Service mockAmazonS3Service = mockAmazonS3Service();
		VerifyCodeGenerator mockedVerifyCodeGenerator = mockedVerifyCodeGenerator();
		VerifyCodeManagementService mockedVerifyCodeManagementService = mockedVerifyCodeManagementService();
		return new MemberService(
			memberRepository,
			mockedEmailService,
			mockAmazonS3Service,
			passwordEncoder,
			watchListRepository,
			watchStockRepository,
			portfolioHoldingRepository,
			portfolioRepository,
			portfolioGainHistoryRepository,
			purchaseHistoryRepository,
			mockedVerifyCodeGenerator,
			notificationPreferenceRepository,
			notificationRepository,
			fcmRepository,
			stockTargetPriceRepository,
			targetPriceNotificationRepository,
			tokenManagementService,
			roleRepository,
			tokenFactory,
			mockedVerifyCodeManagementService
		);
	}

	@Bean
	public AmazonS3Service mockAmazonS3Service() {
		return Mockito.mock(AmazonS3Service.class);
	}

	@Bean
	public VerifyCodeManagementService mockedVerifyCodeManagementService() {
		return Mockito.mock(VerifyCodeManagementService.class);
	}

	@Bean
	public VerifyCodeGenerator mockedVerifyCodeGenerator() {
		return Mockito.mock(VerifyCodeGenerator.class);
	}

	@Bean
	public EmailService mockedEmailService() {
		return Mockito.mock(EmailService.class);
	}
}
