package codesquad.fineants.spring.api.notification.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import codesquad.fineants.domain.fcm_token.FcmRepository;
import codesquad.fineants.domain.fcm_token.FcmToken;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.notification.Notification;
import codesquad.fineants.domain.notification.NotificationRepository;
import codesquad.fineants.domain.notification.type.NotificationType;
import codesquad.fineants.domain.notification_preference.NotificationPreference;
import codesquad.fineants.domain.notification_preference.NotificationPreferenceRepository;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.portfolio_holding.PortfolioHoldingRepository;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.purchase_history.PurchaseHistoryRepository;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.domain.stock_target_price.StockTargetPrice;
import codesquad.fineants.domain.stock_target_price.StockTargetPriceRepository;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotification;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotificationRepository;
import codesquad.fineants.spring.AbstractContainerBaseTest;
import codesquad.fineants.spring.api.firebase.service.FirebaseMessagingService;
import codesquad.fineants.spring.api.kis.client.KisCurrentPrice;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.service.KisService;
import codesquad.fineants.spring.api.notification.manager.NotificationSentManager;
import codesquad.fineants.spring.api.notification.response.PortfolioNotifyMessagesResponse;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotifyMessageResponse;
import reactor.core.publisher.Mono;

class NotificationServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private NotificationService service;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private FcmRepository fcmRepository;

	@Autowired
	private StockTargetPriceRepository stockTargetPriceRepository;

	@Autowired
	private TargetPriceNotificationRepository targetPriceNotificationRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private PortfolioHoldingRepository portfolioHoldingRepository;

	@Autowired
	private PurchaseHistoryRepository purchaseHistoryRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private NotificationPreferenceRepository notificationPreferenceRepository;

	@MockBean
	private NotificationSentManager sentManager;

	@MockBean
	private FirebaseMessaging firebaseMessaging;

	@MockBean
	private CurrentPriceManager manager;

	@MockBean
	private KisService kisService;

	@MockBean
	private FirebaseMessagingService firebaseMessagingService;

	@AfterEach
	void tearDown() {
		notificationRepository.deleteAllInBatch();
		fcmRepository.deleteAllInBatch();
		targetPriceNotificationRepository.deleteAllInBatch();
		stockTargetPriceRepository.deleteAllInBatch();
		purchaseHistoryRepository.deleteAllInBatch();
		portfolioHoldingRepository.deleteAllInBatch();
		portfolioRepository.deleteAllInBatch();
		notificationPreferenceRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
		stockRepository.deleteAllInBatch();
	}

	@DisplayName("포트폴리오 목표 수익률 달성 알림 메시지들을 푸시합니다")
	@Test
	void notifyTargetGainBy() {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(NotificationPreference.builder()
			.browserNotify(true)
			.targetGainNotify(true)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.member(member)
			.build());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding, 100L, 10000.0));

		fcmRepository.save(FcmToken.builder()
			.latestActivationTime(LocalDateTime.now())
			.token(
				"fahY76rRwq8HGy0m1lwckx:APA91bEovbLJyqdSRq8MWDbsIN8sbk90JiNHbIBs6rDoiOKeC-aa5P1QydiRa6okGrIZELrxx_cYieWUN44iX-AD6jma-cYRUR7e3bTMXwkqZFLRZh5s7-bcksGniB7Y2DkoONHtSjos")
			.member(member)
			.build());

		given(firebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("projects/fineants-404407/messages/4754d355-5d5d-4f14-a642-75fecdb91fa5"));
		given(manager.getCurrentPrice(anyString()))
			.willReturn(Optional.of(50000L));

		// when
		PortfolioNotifyMessagesResponse response = service.notifyTargetGainBy(portfolio.getId());

		// then
		assertAll(
			() -> assertThat(response.getNotifications()).hasSize(1),
			() -> assertThat(notificationRepository.findAllByMemberId(member.getId())).hasSize(1)
		);
	}

	@DisplayName("토큰이 유효하지 않아서 목표 수익률 알림을 보낼수 없다")
	@Test
	void notifyTargetGainBy_whenInvalidFcmToken_thenDeleteFcmToken() {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(NotificationPreference.builder()
			.browserNotify(true)
			.targetGainNotify(true)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.member(member)
			.build());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding, 100L, 10000.0));

		FcmToken fcmToken = fcmRepository.save(FcmToken.builder()
			.latestActivationTime(LocalDateTime.now())
			.token(
				"fahY76rRwq8HGy0m1lwckx:APA91bEovbLJyqdSRq8MWDbsIN8sbk90JiNHbIBs6rDoiOKeC-aa5P1QydiRa6okGrIZELrxx_cYieWUN44iX-AD6jma-cYRUR7e3bTMXwkqZFLRZh5s7-bcksGniB7Y2DkoONHtSjos")
			.member(member)
			.build());

		given(firebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.empty());
		given(manager.getCurrentPrice(anyString()))
			.willReturn(Optional.of(50000L));

		// when
		PortfolioNotifyMessagesResponse response = service.notifyTargetGainBy(portfolio.getId());

		// then
		assertAll(
			() -> assertThat(response.getNotifications()).isEmpty(),
			() -> assertThat(fcmRepository.findById(fcmToken.getId())).isEmpty()
		);
	}

	@DisplayName("브라우저 알림 설정이 비활성화되어 목표 수익률 알림을 보낼수 없다")
	@CsvSource(value = {"false,true", "true,false", "false, false"})
	@ParameterizedTest
	void notifyTargetGainBy_whenBrowserNotifyIsInActive_thenResponseEmptyList(boolean browserNotify,
		boolean targetGainNotify) {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(
			createTargetGainNotificationPreference(browserNotify, targetGainNotify, member));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding, 100L, 10000.0));
		fcmRepository.save(createFcmToken("token", member));

		given(manager.getCurrentPrice(anyString()))
			.willReturn(Optional.of(50000L));

		// when
		PortfolioNotifyMessagesResponse response = service.notifyTargetGainBy(portfolio.getId());

		// then
		assertAll(
			() -> assertThat(response.getNotifications()).isEmpty()
		);
	}

	@DisplayName("포트폴리오의 최대 손실율에 도달하여 사용자에게 알림을 푸시합니다")
	@Test
	void notifyPortfolioMaxLossMessages() {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(createNotificationPreference(member));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding, 50L, 60000.0));
		fcmRepository.save(createFcmToken("token", member));

		given(firebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));
		given(manager.getCurrentPrice(anyString()))
			.willReturn(Optional.of(100L));

		// when
		PortfolioNotifyMessagesResponse response = service.notifyMaxLoss(portfolio.getId());

		// then
		assertAll(
			() -> assertThat(response.getNotifications()).hasSize(1),
			() -> assertThat(notificationRepository.findAllByMemberId(member.getId())).hasSize(1)
		);
	}

	@DisplayName("알림 설정이 비활성화 되어 있어서 포트폴리오의 최대 손실율에 도달하여 사용자에게 알림을 푸시할 수 없습니다")
	@CsvSource(value = {"false,true", "true,false", "false, false"})
	@ParameterizedTest
	void notifyMaxLoss_whenNotifySettingIsInActive_thenResponseEmptyList(boolean browserNotify,
		boolean maxLossNotify) {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(
			createMaxLossNotificationPreference(browserNotify, maxLossNotify, member));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding, 50L, 60000.0));
		fcmRepository.save(createFcmToken("token", member));

		given(manager.getCurrentPrice(anyString()))
			.willReturn(Optional.of(50000L));

		// when
		PortfolioNotifyMessagesResponse response = service.notifyMaxLoss(portfolio.getId());

		// then
		assertAll(
			() -> assertThat(response.getNotifications()).isEmpty()
		);
	}

	@DisplayName("토큰이 유효하지 않아서 최대 손실율 달성 알림을 보낼수 없다")
	@Test
	void notifyMaxLoss_whenInvalidFcmToken_thenDeleteFcmToken() throws FirebaseMessagingException {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(NotificationPreference.builder()
			.browserNotify(true)
			.targetGainNotify(true)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.member(member)
			.build());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding, 50L, 60000.0));

		FcmToken fcmToken = fcmRepository.save(FcmToken.builder()
			.latestActivationTime(LocalDateTime.now())
			.token(
				"dcEZXm1dxCV31t-Mt3yikc:APA91bHJv4tQHRaL9P985sCvGOw3b0qr0maz0BXb7_eKOKBZPFM51HytTJMbiUv9L37utFpPNPE5Uxr_VbdUIvmBahOftmVuaNiuOJ35Jk50yKlC-Cj2sQHMwruUZ_O6BjSuGMbrRCi3")
			.member(member)
			.build());

		given(firebaseMessaging.send(any(Message.class)))
			.willThrow(FirebaseMessagingException.class);
		given(manager.getCurrentPrice(anyString()))
			.willReturn(Optional.of(50000L));

		// when
		PortfolioNotifyMessagesResponse response = service.notifyMaxLoss(portfolio.getId());

		// then
		assertAll(
			() -> assertThat(response.getNotifications()).isEmpty(),
			() -> assertThat(fcmRepository.findById(fcmToken.getId())).isEmpty()
		);
	}

	@DisplayName("종목의 현재가가 변경됨에 따라 포트폴리오의 목표 수익률을 달성하여 사용자에게 알림을 전송한다")
	@Test
	void notifyPortfolioTargetGainMessagesByCurrentPrice() {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(createNotificationPreference(member));
		fcmRepository.saveAll(List.of(createFcmToken("token1", member), createFcmToken("token2", member)));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		Stock stock2 = stockRepository.save(createStock2());
		PortfolioHolding holding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		PortfolioHolding holding2 = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock2));
		purchaseHistoryRepository.save(createPurchaseHistory(holding, 100L, 100.0));
		purchaseHistoryRepository.save(createPurchaseHistory(holding2, 1L, 60000.0));

		given(manager.getCurrentPrice(anyString()))
			.willReturn(Optional.of(60000L));
		given(firebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));

		// when
		PortfolioNotifyMessagesResponse response = service.notifyTargetGain();

		// then
		assertAll(
			() -> assertThat(response.getNotifications()).hasSize(1),
			() -> assertThat(notificationRepository.findAllByMemberId(member.getId())).hasSize(1)
		);
	}

	@DisplayName("모든 회원들을 대상으로 특정 티커 심볼에 대한 종목 지정가 알림을 발송한다")
	@Test
	void notifyTargetPrice() {
		// given
		Member member = memberRepository.save(createMember("일개미1234", "kim1234@naver.com"));
		Member member2 = memberRepository.save(createMember("네모네모", "dragonbead95@naver.com"));

		notificationPreferenceRepository.save(createNotificationPreference(member));
		notificationPreferenceRepository.save(createNotificationPreference(member2));

		fcmRepository.save(createFcmToken("token1", member));
		fcmRepository.save(createFcmToken("token2", member2));

		Stock stock = stockRepository.save(createStock());
		Stock stock2 = stockRepository.save(createStock2());

		StockTargetPrice stockTargetPrice1 = stockTargetPriceRepository.save(createStockTargetPrice(member, stock));
		StockTargetPrice stockTargetPrice2 = stockTargetPriceRepository.save(createStockTargetPrice(member, stock2));
		targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice1, List.of(60000L, 70000L)));
		targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice2, List.of(10000L, 20000L)));

		StockTargetPrice stockTargetPrice3 = stockTargetPriceRepository.save(createStockTargetPrice(member2, stock));
		StockTargetPrice stockTargetPrice4 = stockTargetPriceRepository.save(createStockTargetPrice(member2, stock2));
		targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice3, List.of(60000L, 70000L)));
		targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice4, List.of(10000L, 20000L)));

		given(manager.getCurrentPrice(stock.getTickerSymbol()))
			.willReturn(Optional.of(60000L));
		given(manager.getCurrentPrice(stock2.getTickerSymbol()))
			.willReturn(Optional.of(10000L));
		given(kisService.fetchCurrentPrice(stock2.getTickerSymbol()))
			.willReturn(Mono.just(KisCurrentPrice.create(stock2.getTickerSymbol(), 10000L)));
		given(firebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));

		List<String> tickerSymbols = Stream.of(stock, stock2)
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toList());

		// when
		TargetPriceNotifyMessageResponse response = service.notifyTargetPriceBy(tickerSymbols);

		// then
		assertAll(
			() -> assertThat(response.getNotifications())
				.asList()
				.hasSize(4),
			() -> assertThat(notificationRepository.findAllByMemberIds(List.of(member.getId(), member2.getId())))
				.asList()
				.hasSize(4)
		);
	}

	@DisplayName("사용자는 사용자가 지정한 종목 지정가에 대한 푸시 알림을 받는다")
	@Test
	void sendStockTargetPriceNotification() {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(createNotificationPreference(member));
		fcmRepository.save(createFcmToken("token", member));
		fcmRepository.save(createFcmToken("token2", member));
		Stock stock = stockRepository.save(createStock());
		Stock stock2 = stockRepository.save(createStock2());
		StockTargetPrice stockTargetPrice = stockTargetPriceRepository.save(createStockTargetPrice(member, stock));
		StockTargetPrice stockTargetPrice2 = stockTargetPriceRepository.save(createStockTargetPrice(member, stock2));
		targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice, List.of(60000L, 70000L)));
		targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice2, List.of(10000L, 20000L)));

		given(manager.getCurrentPrice(stock.getTickerSymbol()))
			.willReturn(Optional.of(60000L));
		given(manager.getCurrentPrice(stock2.getTickerSymbol()))
			.willReturn(Optional.of(10000L));
		given(firebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));
		// when
		TargetPriceNotifyMessageResponse response = service.notifyTargetPriceBy(
			member.getId());

		// then
		NotificationType type = NotificationType.STOCK_TARGET_PRICE;
		assertThat(response.getNotifications())
			.asList()
			.hasSize(2)
			.extracting("title", "type", "referenceId", "messageId")
			.containsExactlyInAnyOrder(
				Tuple.tuple(type.getName(), type, "005930", "messageId"),
				Tuple.tuple(type.getName(), type, "000020", "messageId"));
		assertThat(notificationRepository.findAllByMemberId(member.getId()))
			.asList()
			.hasSize(2);
	}

	@DisplayName("사용자는 종목 지정가 도달 알림을 받은 상태에서 추가적인 종목 지정가 도달을 하면 알림을 보내지 않는다")
	@Test
	void notifyTargetPrice_whenExistNotification_thenNotSentNotification() {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(NotificationPreference.builder()
			.browserNotify(true)
			.targetGainNotify(true)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.member(member)
			.build());
		fcmRepository.save(createFcmToken("token", member));
		Stock stock = stockRepository.save(createStock());
		Stock stock2 = stockRepository.save(createStock2());
		StockTargetPrice stockTargetPrice = stockTargetPriceRepository.save(createStockTargetPrice(member, stock));
		StockTargetPrice stockTargetPrice2 = stockTargetPriceRepository.save(createStockTargetPrice(member, stock2));
		List<TargetPriceNotification> targetPriceNotifications = createTargetPriceNotification(stockTargetPrice,
			List.of(60000L, 70000L));
		List<TargetPriceNotification> targetPriceNotifications2 = createTargetPriceNotification(stockTargetPrice2,
			List.of(10000L, 20000L));
		targetPriceNotificationRepository.saveAll(targetPriceNotifications);
		targetPriceNotificationRepository.saveAll(targetPriceNotifications2);

		TargetPriceNotification sendTargetPriceNotification = targetPriceNotifications.get(0);
		notificationRepository.save(Notification.stock(
			sendTargetPriceNotification.getStockTargetPrice().getStock().getTickerSymbol(),
			sendTargetPriceNotification.getTargetPrice(),
			"종목 지정가",
			sendTargetPriceNotification.getStockTargetPrice().getStock().getTickerSymbol(),
			"messageId",
			sendTargetPriceNotification.getId(),
			member
		));

		given(manager.getCurrentPrice(stock.getTickerSymbol()))
			.willReturn(Optional.of(60000L));
		given(manager.getCurrentPrice(stock2.getTickerSymbol()))
			.willReturn(Optional.of(10000L));
		given(sentManager.hasTargetPriceSendHistory(sendTargetPriceNotification.getId()))
			.willReturn(true);
		given(firebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));
		// when
		TargetPriceNotifyMessageResponse response = service.notifyTargetPriceBy(
			List.of(stock.getTickerSymbol(), stock2.getTickerSymbol()));

		// then
		NotificationType type = NotificationType.STOCK_TARGET_PRICE;
		assertThat(response.getNotifications())
			.asList()
			.hasSize(1)
			.extracting("title", "type", "referenceId", "messageId")
			.containsExactlyInAnyOrder(
				Tuple.tuple(type.getName(), type, "000020", "messageId"));
		assertThat(notificationRepository.findAllByMemberId(member.getId()))
			.asList()
			.hasSize(2);
	}

	@DisplayName("종목 지정가 도달 알림을 보내는데 실패하면 알림을 저장하지 않아야 한다")
	@Test
	void notifyTargetPrice_whenFailSendingNotification_thenNotSaveNotification() {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(NotificationPreference.builder()
			.browserNotify(true)
			.targetGainNotify(true)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.member(member)
			.build());
		fcmRepository.save(createFcmToken("token", member));
		Stock stock = stockRepository.save(createStock());
		StockTargetPrice stockTargetPrice = stockTargetPriceRepository.save(createStockTargetPrice(member, stock));
		List<TargetPriceNotification> targetPriceNotifications = createTargetPriceNotification(stockTargetPrice,
			List.of(60000L, 70000L));
		targetPriceNotificationRepository.saveAll(targetPriceNotifications);

		given(manager.getCurrentPrice(stock.getTickerSymbol()))
			.willReturn(Optional.of(60000L));
		given(firebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.empty());
		// when
		TargetPriceNotifyMessageResponse response = service.notifyTargetPriceBy(
			List.of(stock.getTickerSymbol()));

		// then
		NotificationType type = NotificationType.STOCK_TARGET_PRICE;
		assertThat(response.getNotifications())
			.asList()
			.isEmpty();
		assertThat(notificationRepository.findAllByMemberId(member.getId()))
			.asList()
			.hasSize(0);
	}

	@DisplayName("티커 심볼을 기준으로 종목 지정가 알림을 발송한다")
	@Test
	void notifyTargetPrice_whenMultipleMember_thenSendNotification() {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(NotificationPreference.builder()
			.browserNotify(true)
			.targetGainNotify(true)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.member(member)
			.build());
		fcmRepository.save(createFcmToken("token", member));
		Stock stock = stockRepository.save(createStock());
		Stock stock2 = stockRepository.save(createStock2());
		StockTargetPrice stockTargetPrice = stockTargetPriceRepository.save(createStockTargetPrice(member, stock));
		StockTargetPrice stockTargetPrice2 = stockTargetPriceRepository.save(createStockTargetPrice(member, stock2));
		targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice, List.of(60000L, 70000L)));
		targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice2, List.of(10000L, 20000L)));

		given(manager.getCurrentPrice(stock.getTickerSymbol()))
			.willReturn(Optional.of(60000L));
		given(manager.getCurrentPrice(stock2.getTickerSymbol()))
			.willReturn(Optional.of(10000L));
		given(sentManager.hasTargetPriceSendHistory(anyLong()))
			.willReturn(false);
		given(firebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));
		// when
		TargetPriceNotifyMessageResponse response = service.notifyTargetPriceBy(
			List.of(stock.getTickerSymbol(), stock2.getTickerSymbol()));

		// then
		NotificationType type = NotificationType.STOCK_TARGET_PRICE;
		assertThat(response.getNotifications())
			.asList()
			.hasSize(2)
			.extracting("title", "type", "referenceId", "messageId")
			.containsExactlyInAnyOrder(
				Tuple.tuple(type.getName(), type, "005930", "messageId"),
				Tuple.tuple(type.getName(), type, "000020", "messageId"));
		assertThat(notificationRepository.findAllByMemberId(member.getId()))
			.asList()
			.hasSize(2);
	}

	private FcmToken createFcmToken(String token, Member member) {
		return FcmToken.builder()
			.token(token)
			.latestActivationTime(LocalDateTime.now())
			.member(member)
			.build();
	}

	private Member createMember() {
		return createMember("일개미1234", "dragonbead95@naver.com");
	}

	private Member createMember(String nickname, String email) {
		return Member.builder()
			.nickname(nickname)
			.email(email)
			.password("kim1234@")
			.provider("local")
			.build();
	}

	private Portfolio createPortfolio(Member member) {
		return Portfolio.builder()
			.name("내꿈은 워렌버핏")
			.securitiesFirm("토스")
			.budget(1000000L)
			.targetGain(1500000L)
			.maximumLoss(900000L)
			.member(member)
			.targetGainIsActive(true)
			.maximumLossIsActive(true)
			.build();
	}

	private Stock createStock() {
		return Stock.builder()
			.companyName("삼성전자보통주")
			.tickerSymbol("005930")
			.companyNameEng("SamsungElectronics")
			.stockCode("KR7005930003")
			.sector("전기전자")
			.market(Market.KOSPI)
			.build();
	}

	private PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock) {
		return PortfolioHolding.builder()
			.portfolio(portfolio)
			.stock(stock)
			.build();
	}

	private PurchaseHistory createPurchaseHistory(PortfolioHolding portfolioHolding, Long numShares,
		Double purchasePricePerShare) {
		return PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.of(2023, 9, 26, 9, 30, 0))
			.numShares(numShares)
			.purchasePricePerShare(purchasePricePerShare)
			.memo("첫구매")
			.portfolioHolding(portfolioHolding)
			.build();
	}

	private StockTargetPrice createStockTargetPrice(Member member, Stock stock) {
		return StockTargetPrice.builder()
			.member(member)
			.stock(stock)
			.isActive(true)
			.build();
	}

	private TargetPriceNotification createTargetPriceNotification(StockTargetPrice stockTargetPrice, Long targetPrice) {
		return TargetPriceNotification.builder()
			.targetPrice(targetPrice)
			.stockTargetPrice(stockTargetPrice)
			.build();
	}

	private List<TargetPriceNotification> createTargetPriceNotification(StockTargetPrice stockTargetPrice,
		List<Long> targetPrices) {
		return targetPrices.stream()
			.map(targetPrice -> TargetPriceNotification.builder()
				.targetPrice(targetPrice)
				.stockTargetPrice(stockTargetPrice)
				.build())
			.collect(Collectors.toList());
	}

	private NotificationPreference createNotificationPreference(Member member) {
		return NotificationPreference.builder()
			.browserNotify(true)
			.targetGainNotify(true)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.member(member)
			.build();
	}

	private NotificationPreference createTargetGainNotificationPreference(boolean browserNotify,
		boolean targetGainNotify,
		Member member) {
		return NotificationPreference.builder()
			.browserNotify(browserNotify)
			.targetGainNotify(targetGainNotify)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.member(member)
			.build();
	}

	private NotificationPreference createMaxLossNotificationPreference(boolean browserNotify, boolean maxLossNotify,
		Member member) {
		return NotificationPreference.builder()
			.browserNotify(browserNotify)
			.targetGainNotify(true)
			.maxLossNotify(maxLossNotify)
			.targetPriceNotify(true)
			.member(member)
			.build();
	}

	private Stock createStock2() {
		return Stock.builder()
			.companyName("동화약품보통주")
			.tickerSymbol("000020")
			.companyNameEng("DongwhaPharm")
			.stockCode("KR7000020008")
			.sector("의약품")
			.market(Market.KOSPI)
			.build();
	}
}
