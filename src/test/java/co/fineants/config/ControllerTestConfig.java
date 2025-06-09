package co.fineants.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

import co.fineants.api.domain.fcm.service.FcmService;
import co.fineants.api.domain.holding.domain.factory.PortfolioSseEmitterFactory;
import co.fineants.api.domain.holding.domain.factory.PortfolioStreamMessageConsumerFactory;
import co.fineants.api.domain.holding.domain.factory.PortfolioStreamerFactory;
import co.fineants.api.domain.holding.domain.factory.SseEventBuilderFactory;
import co.fineants.api.domain.holding.service.PortfolioHoldingService;
import co.fineants.api.domain.holding.service.market_status_checker.MarketStatusCheckerRule;
import co.fineants.api.domain.holding.service.streamer.PortfolioStreamer;
import co.fineants.api.domain.member.service.MemberNotificationPreferenceService;
import co.fineants.api.domain.member.service.MemberNotificationService;
import co.fineants.api.domain.member.service.MemberService;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.portfolio.service.PortFolioService;
import co.fineants.api.domain.portfolio.service.PortfolioNotificationService;
import co.fineants.api.domain.portfolio.service.PortfolioNotificationSettingService;
import co.fineants.api.domain.purchasehistory.service.PurchaseHistoryService;
import co.fineants.api.domain.stock.service.StockService;
import co.fineants.api.domain.stock_target_price.service.StockTargetPriceService;
import co.fineants.api.domain.stock_target_price.service.TargetPriceNotificationService;
import co.fineants.api.domain.watchlist.service.WatchListService;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.global.security.oauth.resolver.MemberAuthenticationArgumentResolver;

@TestConfiguration
public class ControllerTestConfig {
	@MockBean
	private FcmService fcmService;

	@MockBean
	private MemberNotificationService notificationService;

	@MockBean
	private MemberNotificationPreferenceService preferenceService;

	@MockBean
	private MemberService memberService;

	@MockBean
	private PortFolioService portFolioService;

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
	protected MemberAuthenticationArgumentResolver memberAuthenticationArgumentResolver;

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
}
