package co.fineants.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

import co.fineants.api.domain.fcm.service.FcmService;
import co.fineants.api.domain.holding.domain.factory.PortfolioSseEmitterFactory;
import co.fineants.api.domain.holding.domain.factory.PortfolioStreamMessageConsumerFactory;
import co.fineants.api.domain.holding.domain.factory.PortfolioStreamerFactory;
import co.fineants.api.domain.holding.domain.factory.SseEventBuilderFactory;
import co.fineants.api.domain.holding.event.publisher.PortfolioHoldingEventPublisher;
import co.fineants.api.domain.holding.service.PortfolioHoldingFacade;
import co.fineants.api.domain.holding.service.PortfolioHoldingService;
import co.fineants.api.domain.holding.service.market_status_checker.MarketStatusCheckerRule;
import co.fineants.api.domain.holding.service.streamer.PortfolioStreamer;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.portfolio.service.PortfolioCacheService;
import co.fineants.api.domain.portfolio.service.PortfolioNotificationService;
import co.fineants.api.domain.portfolio.service.PortfolioNotificationSettingService;
import co.fineants.api.domain.portfolio.service.PortfolioService;
import co.fineants.api.domain.purchasehistory.service.PurchaseHistoryService;
import co.fineants.api.domain.stock.service.StockService;
import co.fineants.api.domain.stock_target_price.service.StockTargetPriceService;
import co.fineants.api.domain.stock_target_price.service.TargetPriceNotificationService;
import co.fineants.api.domain.watchlist.service.WatchListService;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.global.security.oauth.resolver.MemberAuthenticationArgumentResolver;
import co.fineants.member.application.ChangeMemberPassword;
import co.fineants.member.application.ChangeMemberProfile;
import co.fineants.member.application.LogoutMember;
import co.fineants.member.application.MemberNotificationPreferenceService;
import co.fineants.member.application.MemberNotificationService;
import co.fineants.member.application.ReadMemberProfile;

@TestConfiguration
public class ControllerTestConfig {
	@MockBean
	protected MemberAuthenticationArgumentResolver memberAuthenticationArgumentResolver;
	@MockBean
	private FcmService fcmService;
	@MockBean
	private MemberNotificationService notificationService;
	@MockBean
	private MemberNotificationPreferenceService preferenceService;
	@MockBean
	private PortfolioService portFolioService;
	@MockBean
	private PortfolioHoldingService portfolioHoldingService;
	@MockBean
	private LocalDateTimeService localDateTimeService;
	@MockBean
	private PortfolioNotificationService portfolioNotificationService;
	@MockBean
	private PortfolioRepository portfolioRepository;
	@MockBean
	private PortfolioNotificationSettingService portfolioNotificationSettingService;
	@MockBean
	private PurchaseHistoryService purchaseHistoryService;
	@MockBean
	private StockService stockService;
	@MockBean
	private StockTargetPriceService stockTargetPriceService;
	@MockBean
	private TargetPriceNotificationService targetPriceNotificationService;
	@MockBean
	private WatchListService watchListService;
	@MockBean
	private PortfolioStreamer portfolioStreamer;

	@MockBean
	private MarketStatusCheckerRule stockMarketChecker;

	@MockBean
	private PortfolioStreamerFactory portfolioStreamerFactory;

	@MockBean
	private PortfolioStreamMessageConsumerFactory portfolioStreamMessageConsumerFactory;

	@MockBean
	private PortfolioSseEmitterFactory portfolioSseEmitterFactory;

	@MockBean
	private SseEventBuilderFactory sseEventBuilderFactory;

	@MockBean
	private PortfolioCacheService portfolioCacheService;

	@MockBean
	private PortfolioHoldingEventPublisher portfolioHoldingEventPublisher;

	@MockBean
	private PortfolioHoldingFacade portfolioHoldingFacade;

	@MockBean
	private LogoutMember logoutMember;

	@MockBean
	private ChangeMemberProfile changeMemberProfile;

	@MockBean
	private ChangeMemberPassword changeMemberPassword;

	@MockBean
	private ReadMemberProfile readMemberProfile;
}
