package co.fineants.api.domain.notification.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.fcm.domain.entity.FcmToken;
import co.fineants.api.domain.fcm.repository.FcmRepository;
import co.fineants.api.domain.fcm.service.FirebaseMessagingService;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessageItem;
import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.domain.entity.type.NotificationType;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.domain.notification.repository.NotificationSentRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
import co.fineants.api.domain.stock_target_price.domain.entity.TargetPriceNotification;
import co.fineants.api.domain.stock_target_price.repository.StockTargetPriceRepository;
import co.fineants.api.domain.stock_target_price.repository.TargetPriceNotificationRepository;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberEmail;
import co.fineants.member.domain.NotificationPreference;
import co.fineants.member.infrastructure.MemberRepository;
import reactor.core.publisher.Mono;

class NotificationServiceTest extends co.fineants.AbstractContainerBaseTest {

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
	private NotificationSentRepository sentManager;

	@Autowired
	private FirebaseMessaging firebaseMessaging;

	@Autowired
	private CurrentPriceRedisRepository manager;

	@Autowired
	private KisService mockedKisService;

	@Autowired
	private FirebaseMessagingService mockedFirebaseMessagingService;

	@DisplayName("포트폴리오 목표 수익률 달성 알림 메시지들을 푸시합니다")
	@Test
	void notifyTargetGainBy() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(100);
		Money purchasePricePerShare = Money.won(10000);
		String memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePricePerShare, memo, portfolioHolding));

		fcmRepository.save(createFcmToken("fcmToken", member));

		given(mockedFirebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("projects/fineants-404407/messages/4754d355-5d5d-4f14-a642-75fecdb91fa5"));
		manager.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 50000L));

		// when
		List<NotifyMessageItem> actual = service.notifyTargetGain(portfolio.getId());

		// then
		assertAll(
			() -> assertThat(actual).hasSize(1),
			() -> assertThat(notificationRepository.findAllByMemberId(member.getId())).hasSize(1),
			() -> assertThat(sentManager.hasTargetGainSendHistory(portfolio.getId())).isTrue()
		);
	}

	@DisplayName("목표수익률에 도달하지 않아서 알림을 보내지 않는다")
	@Test
	void notifyTargetGainBy_whenNoTargetGain_thenNotSendNotification() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(
			createPortfolio(member, "내꿈은 워렌버핏", Money.won(1000000L), Money.won(1100000L), Money.won(900000L)));
		Stock samsung = stockRepository.save(createSamsungStock());
		Stock ccs = stockRepository.save(createCcsStack());

		PortfolioHolding samsungHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, samsung));
		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(12);
		Money purchasePricePerShare = Money.won(60000);
		String memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePricePerShare, memo, samsungHolding));

		PortfolioHolding ccsHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, ccs));
		purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		numShares = Count.from(15);
		purchasePricePerShare = Money.won(2000);
		memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePricePerShare, memo, ccsHolding));

		fcmRepository.save(createFcmToken("fcmToken", member));

		given(mockedFirebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("projects/fineants-404407/messages/4754d355-5d5d-4f14-a642-75fecdb91fa5"));
		manager.savePrice(KisCurrentPrice.create(samsung.getTickerSymbol(), 83300L));
		manager.savePrice(KisCurrentPrice.create(ccs.getTickerSymbol(), 3750L));

		// when
		List<NotifyMessageItem> actual = service.notifyTargetGain(portfolio.getId());

		// then
		assertAll(
			() -> assertThat(actual).isEmpty(),
			() -> assertThat(notificationRepository.findAllByMemberId(member.getId())).isEmpty(),
			() -> assertThat(sentManager.hasTargetGainSendHistory(portfolio.getId())).isFalse()
		);
	}

	@DisplayName("토큰이 유효하지 않아서 목표 수익률 알림을 보낼수 없지만, 알림은 저장된다")
	@Test
	void notifyTargetGainBy_whenInvalidFcmToken_thenDeleteFcmToken() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());

		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(100);
		Money purchasePricePerShare = Money.won(10000);
		String memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePricePerShare, memo, portfolioHolding));

		FcmToken fcmToken = fcmRepository.save(createFcmToken("fcmToken", member));

		given(mockedFirebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.empty());
		manager.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 50000L));

		// when
		List<NotifyMessageItem> actual = service.notifyTargetGain(portfolio.getId());

		// then
		assertAll(
			() -> assertThat(actual).hasSize(1),
			() -> assertThat(fcmRepository.findById(fcmToken.getId())).isEmpty(),
			() -> assertThat(sentManager.hasTargetGainSendHistory(portfolio.getId())).isTrue()
		);
	}

	@DisplayName("브라우저 알림 설정이 비활성화되어 목표 수익률 알림을 보낼수 없다")
	@CsvSource(value = {"false,true", "true,false", "false, false"})
	@ParameterizedTest
	void notifyTargetGainBy_whenBrowserNotifyIsInActive_thenResponseEmptyList(boolean browserNotify,
		boolean targetGainNotify) {
		// given
		Member member = createMember();
		NotificationPreference changePreference = createNotificationPreference(browserNotify, targetGainNotify, true,
			true);
		member.setNotificationPreference(changePreference);
		member = memberRepository.save(member);

		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());

		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(100);
		Money purchasePricePerShare = Money.won(10000);
		String memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePricePerShare, memo, portfolioHolding));
		fcmRepository.save(createFcmToken("token", member));

		manager.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 50000L));

		// when
		List<NotifyMessageItem> actual = service.notifyTargetGain(portfolio.getId());

		// then
		assertAll(
			() -> assertThat(actual).isEmpty()
		);
	}

	@DisplayName("모든 포트폴리오의 최대 손실율 도달을 만족하는 회원들에게 알림을 푸시한다")
	@Test
	void notifyMaxLossAll() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(50);
		Money purchasePricePerShare = Money.won(60000);
		String memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePricePerShare, memo, portfolioHolding));
		fcmRepository.save(createFcmToken("token", member));

		given(mockedFirebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));
		manager.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 100L));

		// when
		List<NotifyMessageItem> actual = service.notifyMaxLossAll();

		// then
		assertAll(
			() -> assertThat(actual).hasSize(1),
			() -> assertThat(notificationRepository.findAllByMemberId(member.getId())).hasSize(1),
			() -> assertThat(sentManager.hasMaxLossSendHistory(portfolio.getId())).isTrue()
		);
	}

	@DisplayName("포트폴리오의 최대 손실율에 도달하여 사용자에게 알림을 푸시합니다")
	@Test
	void notifyPortfolioMaxLossMessages() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(50);
		Money purchasePricePerShare = Money.won(60000);
		String memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePricePerShare, memo, portfolioHolding));
		fcmRepository.save(createFcmToken("token", member));

		given(mockedFirebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));
		manager.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 100L));

		// when
		List<NotifyMessageItem> actual = service.notifyMaxLoss(portfolio.getId());

		// then
		assertAll(
			() -> assertThat(actual).hasSize(1),
			() -> assertThat(notificationRepository.findAllByMemberId(member.getId())).hasSize(1),
			() -> assertThat(sentManager.hasMaxLossSendHistory(portfolio.getId())).isTrue()
		);
	}

	@DisplayName("알림 설정이 비활성화 되어 있어서 포트폴리오의 최대 손실율에 도달하여 사용자에게 알림을 푸시할 수 없습니다")
	@CsvSource(value = {"false,true", "true,false", "false, false"})
	@ParameterizedTest
	void notifyMaxLoss_whenNotifySettingIsInActive_thenResponseEmptyList(boolean browserNotify, boolean maxLossNotify) {
		// given
		Member member = createMember();
		NotificationPreference changePreference = createNotificationPreference(browserNotify, true, maxLossNotify,
			true);
		member.setNotificationPreference(changePreference);
		member = memberRepository.save(member);

		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(50);
		Money purchasePricePerShare = Money.won(60000);
		String memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePricePerShare, memo, portfolioHolding));
		fcmRepository.save(createFcmToken("token", member));

		manager.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 50000L));

		// when
		List<NotifyMessageItem> actual = service.notifyMaxLoss(portfolio.getId());

		// then
		assertAll(
			() -> assertThat(actual).isEmpty(),
			() -> assertThat(sentManager.hasMaxLossSendHistory(portfolio.getId())).isFalse()
		);
	}

	@SuppressWarnings("checkstyle:OneStatementPerLine")
	@DisplayName("토큰이 유효하지 않아서 최대 손실율 달성 알림을 보낼수 없지만, 알림은 저장된다")
	@Test
	void notifyMaxLoss_whenInvalidFcmToken_thenDeleteFcmToken() throws FirebaseMessagingException {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(10);
		Money purchasePricePerShare = Money.won(60000);
		String memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePricePerShare, memo, portfolioHolding));

		FcmToken fcmToken = fcmRepository.save(createFcmToken("fcmToken", member));

		given(firebaseMessaging.send(any(Message.class)))
			.willThrow(FirebaseMessagingException.class);
		manager.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 50000L));

		// when
		List<NotifyMessageItem> actual = service.notifyMaxLoss(portfolio.getId());

		// then
		assertAll(
			() -> assertThat(actual).hasSize(1),
			() -> assertThat(fcmRepository.findById(fcmToken.getId())).isEmpty(),
			() -> assertThat(sentManager.hasMaxLossSendHistory(portfolio.getId())).isTrue()
		);
	}

	@DisplayName("종목의 현재가가 변경됨에 따라 포트폴리오의 목표 수익률을 달성하여 사용자에게 알림을 전송한다")
	@Test
	void notifyPortfolioTargetGainMessagesByCurrentPrice() {
		// given
		Member member = memberRepository.save(createMember());
		fcmRepository.saveAll(List.of(createFcmToken("token1", member), createFcmToken("token2", member)));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		Stock stock2 = stockRepository.save(createDongwhaPharmStock());
		PortfolioHolding holding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		PortfolioHolding holding2 = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock2));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(100);
		Money purchasePricePerShare = Money.won(100);
		String memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePricePerShare, memo, holding));

		purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		numShares = Count.from(1);
		purchasePricePerShare = Money.won(60000);
		memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePricePerShare, memo, holding2));

		manager.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 60000L));
		manager.savePrice(KisCurrentPrice.create(stock2.getTickerSymbol(), 60000L));
		given(mockedFirebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));

		// when
		List<NotifyMessageItem> items = service.notifyTargetGainAll();

		// then
		assertAll(
			() -> assertThat(items).hasSize(1),
			() -> assertThat(notificationRepository.findAllByMemberId(member.getId())).hasSize(1),
			() -> assertThat(sentManager.hasTargetGainSendHistory(portfolio.getId())).isTrue()
		);
	}

	@DisplayName("모든 회원들을 대상으로 특정 티커 심볼에 대한 종목 지정가 알림을 발송한다")
	@Test
	void notifyTargetPrice() {
		// given
		Member member = memberRepository.save(createMember("일개미1234", "kim1234@naver.com"));
		Member member2 = memberRepository.save(createMember("네모네모", "dragonbead95@naver.com"));

		fcmRepository.save(createFcmToken("token1", member));
		fcmRepository.save(createFcmToken("token2", member2));

		Stock stock = stockRepository.save(createSamsungStock());
		Stock stock2 = stockRepository.save(createDongwhaPharmStock());

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

		manager.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 60000L));
		manager.savePrice(KisCurrentPrice.create(stock2.getTickerSymbol(), 10000L));
		given(mockedKisService.fetchCurrentPrice(stock2.getTickerSymbol()))
			.willReturn(Mono.just(KisCurrentPrice.create(stock2.getTickerSymbol(), 10000L)));
		given(mockedFirebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));

		List<String> tickerSymbols = Stream.of(stock, stock2)
			.map(Stock::getTickerSymbol)
			.toList();

		// when
		List<NotifyMessageItem> actual = service.notifyTargetPriceBy(tickerSymbols);

		// then
		assertAll(
			() -> assertThat(actual).hasSize(4),
			() -> assertThat(notificationRepository.findAllByMemberIds(List.of(member.getId(), member2.getId())))
				.asList()
				.hasSize(4)
		);
	}

	@DisplayName("종목 지정가 알림 발송 시나리오")
	@TestFactory
	Collection<DynamicTest> createNotifyTargetPriceDynamicTest() {
		return List.of(
			DynamicTest.dynamicTest("종목 지정가 알림을 전송한다", () -> {
				// given
				Member member = memberRepository.save(createMember("네모네모", "dragonbead95@naver.com"));
				fcmRepository.save(createFcmToken("token1", member));
				Stock stock = stockRepository.save(createSamsungStock());

				StockTargetPrice stockTargetPrice1 = stockTargetPriceRepository.save(
					createStockTargetPrice(member, stock));
				targetPriceNotificationRepository.saveAll(
					createTargetPriceNotification(stockTargetPrice1, List.of(60000L, 70000L)));

				manager.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 60000L));
				given(mockedKisService.fetchCurrentPrice(stock.getTickerSymbol()))
					.willReturn(Mono.just(KisCurrentPrice.create(stock.getTickerSymbol(), 10000L)));
				given(mockedFirebaseMessagingService.send(any(Message.class)))
					.willReturn(Optional.of("messageId"));

				List<String> tickerSymbols = Stream.of(stock)
					.map(Stock::getTickerSymbol)
					.toList();

				// when
				List<NotifyMessageItem> actual = service.notifyTargetPriceBy(tickerSymbols);

				// then
				assertAll(
					() -> assertThat(actual).hasSize(1),
					() -> assertThat(
						notificationRepository.findAllByMemberIds(List.of(member.getId())))
						.asList()
						.hasSize(1)
				);
			}),
			DynamicTest.dynamicTest("전송 이력이 있어서 알림을 받지 않는다", () -> {
				// given
				MemberEmail memberEmail = new MemberEmail("dragonbead95@naver.com");
				Member member = memberRepository.findMemberByEmailAndProvider(memberEmail, "local")
					.orElseThrow();
				Stock stock = createSamsungStock();
				List<String> tickerSymbols = Stream.of(stock)
					.map(Stock::getTickerSymbol)
					.toList();

				// when
				List<NotifyMessageItem> actual = service.notifyTargetPriceBy(tickerSymbols);

				// then
				assertAll(
					() -> assertThat(actual).isEmpty(),
					() -> assertThat(
						notificationRepository.findAllByMemberIds(List.of(member.getId())))
						.asList()
						.hasSize(1)
				);
			})
		);
	}

	@DisplayName("사용자는 사용자가 지정한 종목 지정가에 대한 푸시 알림을 받는다")
	@Test
	void sendStockTargetPriceNotification() {
		// given
		Member member = memberRepository.save(createMember());
		fcmRepository.save(createFcmToken("token", member));
		fcmRepository.save(createFcmToken("token2", member));
		Stock stock = stockRepository.save(createSamsungStock());
		Stock stock2 = stockRepository.save(createDongwhaPharmStock());
		StockTargetPrice stockTargetPrice = stockTargetPriceRepository.save(createStockTargetPrice(member, stock));
		StockTargetPrice stockTargetPrice2 = stockTargetPriceRepository.save(createStockTargetPrice(member, stock2));
		List<TargetPriceNotification> targetPriceNotifications = targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice, List.of(60000L, 70000L)));
		List<TargetPriceNotification> targetPriceNotifications2 = targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice2, List.of(10000L, 20000L)));

		manager.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 60000L));
		manager.savePrice(KisCurrentPrice.create(stock2.getTickerSymbol(), 10000L));
		given(mockedFirebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));
		// when
		List<NotifyMessageItem> actual = service.notifyTargetPrice(member.getId());

		// then
		NotifyMessageItem expected1 = NotifyMessageItem.targetPriceNotifyMessageItem(
			1L,
			false,
			"종목 지정가",
			"삼성전자보통주이(가) ₩60,000에 도달했습니다",
			NotificationType.STOCK_TARGET_PRICE,
			"005930",
			member.getId(),
			"/stock/005930",
			List.of("messageId", "messageId"),
			"삼성전자보통주",
			Money.won(60000),
			targetPriceNotifications.get(0).getId()
		);
		NotifyMessageItem expected2 = NotifyMessageItem.targetPriceNotifyMessageItem(
			2L,
			false,
			"종목 지정가",
			"동화약품보통주이(가) ₩10,000에 도달했습니다",
			NotificationType.STOCK_TARGET_PRICE,
			"000020",
			member.getId(),
			"/stock/000020",
			List.of("messageId", "messageId"),
			"동화약품보통주",
			Money.won(10000),
			targetPriceNotifications2.get(0).getId()
		);

		assertThat(actual)
			.hasSize(2)
			.usingComparatorForType(Money::compareTo, Money.class)
			.containsExactly(expected1, expected2);
		assertThat(notificationRepository.findAllByMemberId(member.getId()))
			.asList()
			.hasSize(2);
	}

	@DisplayName("사용자는 종목 지정가 도달 알림을 받은 상태에서 추가적인 종목 지정가 도달을 하면 알림을 보내지 않는다")
	@Test
	void notifyTargetPrice_whenExistNotification_thenNotSentNotification() {
		// given
		Member member = memberRepository.save(createMember());
		fcmRepository.save(createFcmToken("token", member));
		Stock stock = stockRepository.save(createSamsungStock());
		Stock stock2 = stockRepository.save(createDongwhaPharmStock());
		StockTargetPrice stockTargetPrice = stockTargetPriceRepository.save(createStockTargetPrice(member, stock));
		StockTargetPrice stockTargetPrice2 = stockTargetPriceRepository.save(createStockTargetPrice(member, stock2));
		List<TargetPriceNotification> targetPriceNotifications = createTargetPriceNotification(stockTargetPrice,
			List.of(60000L, 70000L));
		List<TargetPriceNotification> targetPriceNotifications2 = createTargetPriceNotification(stockTargetPrice2,
			List.of(10000L, 20000L));
		targetPriceNotificationRepository.saveAll(targetPriceNotifications);
		targetPriceNotificationRepository.saveAll(targetPriceNotifications2);

		TargetPriceNotification sendTargetPriceNotification = targetPriceNotifications.get(0);
		Notification notification = notificationRepository.save(Notification.stockTargetPriceNotification(
			"종목 지정가", sendTargetPriceNotification.getStockTargetPrice().getStock().getTickerSymbol(),
			"/stock/" + sendTargetPriceNotification.getStockTargetPrice().getStock().getTickerSymbol(), member,
			List.of("messageId"), sendTargetPriceNotification.getStockTargetPrice().getStock().getTickerSymbol(),
			sendTargetPriceNotification.getTargetPrice(),
			sendTargetPriceNotification.getId()
		));

		manager.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 60000L));
		manager.savePrice(KisCurrentPrice.create(stock2.getTickerSymbol(), 10000L));
		sentManager.addTargetPriceSendHistory(notification);
		given(mockedFirebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));
		// when
		List<NotifyMessageItem> actual = service.notifyTargetPriceBy(
			List.of(stock.getTickerSymbol(), stock2.getTickerSymbol()));

		// then
		NotifyMessageItem expected1 = NotifyMessageItem.targetPriceNotifyMessageItem(
			2L,
			false,
			"종목 지정가",
			"동화약품보통주이(가) ₩10,000에 도달했습니다",
			NotificationType.STOCK_TARGET_PRICE,
			"000020",
			member.getId(),
			"/stock/000020",
			List.of("messageId"),
			"동화약품보통주",
			Money.won(10_000),
			targetPriceNotifications2.get(0).getId()
		);
		assertThat(actual)
			.hasSize(1)
			.containsExactly(expected1);
		assertThat(notificationRepository.findAllByMemberId(member.getId()))
			.asList()
			.hasSize(2);
	}

	@DisplayName("종목 지정가 도달 알림을 보내는데 실패해도 알림은 저장되어야 한다")
	@Test
	void notifyTargetPrice_whenFailSendingNotification_thenSaveNotification() {
		// given
		Member member = memberRepository.save(createMember());
		fcmRepository.save(createFcmToken("token", member));
		Stock stock = stockRepository.save(createSamsungStock());
		StockTargetPrice stockTargetPrice = stockTargetPriceRepository.save(createStockTargetPrice(member, stock));
		List<TargetPriceNotification> targetPriceNotifications = createTargetPriceNotification(stockTargetPrice,
			List.of(60000L, 70000L));
		targetPriceNotificationRepository.saveAll(targetPriceNotifications);

		manager.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 60000L));
		given(mockedFirebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.empty());
		// when
		List<NotifyMessageItem> actual = service.notifyTargetPriceBy(List.of(stock.getTickerSymbol()));

		// then
		assertThat(actual)
			.asList()
			.hasSize(1);
		assertThat(notificationRepository.findAllByMemberId(member.getId()))
			.asList()
			.hasSize(1);
	}

	@DisplayName("티커 심볼을 기준으로 종목 지정가 알림을 발송한다")
	@Test
	void notifyTargetPrice_whenMultipleMember_thenSendNotification() {
		// given
		Member member = memberRepository.save(createMember());
		fcmRepository.save(createFcmToken("token", member));
		Stock stock = stockRepository.save(createSamsungStock());
		Stock stock2 = stockRepository.save(createDongwhaPharmStock());
		StockTargetPrice stockTargetPrice = stockTargetPriceRepository.save(createStockTargetPrice(member, stock));
		StockTargetPrice stockTargetPrice2 = stockTargetPriceRepository.save(createStockTargetPrice(member, stock2));
		List<TargetPriceNotification> targetPriceNotifications = targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice, List.of(60000L, 70000L)));
		List<TargetPriceNotification> targetPriceNotifications2 = targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice2, List.of(10000L, 20000L)));

		manager.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 60000L));
		manager.savePrice(KisCurrentPrice.create(stock2.getTickerSymbol(), 10000L));
		given(mockedFirebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));
		// when
		List<NotifyMessageItem> actual = service.notifyTargetPriceBy(
			List.of(stock.getTickerSymbol(), stock2.getTickerSymbol()));

		// then
		NotifyMessageItem expected1 = NotifyMessageItem.targetPriceNotifyMessageItem(
			1L,
			false,
			"종목 지정가",
			"삼성전자보통주이(가) ₩60,000에 도달했습니다",
			NotificationType.STOCK_TARGET_PRICE,
			"005930",
			member.getId(),
			"/stock/005930",
			List.of("messageId"),
			"삼성전자보통주",
			Money.won(60_000),
			targetPriceNotifications.get(0).getId()
		);
		NotifyMessageItem expected2 = NotifyMessageItem.targetPriceNotifyMessageItem(
			2L,
			false,
			"종목 지정가",
			"동화약품보통주이(가) ₩10,000에 도달했습니다",
			NotificationType.STOCK_TARGET_PRICE,
			"000020",
			member.getId(),
			"/stock/000020",
			List.of("messageId"),
			"동화약품보통주",
			Money.won(10_000),
			targetPriceNotifications2.get(0).getId()
		);
		assertThat(actual)
			.hasSize(2)
			.containsExactly(expected1, expected2);
		assertThat(notificationRepository.findAllByMemberId(member.getId()))
			.asList()
			.hasSize(2);
	}

	@DisplayName("조건을 만족한 포트폴리오에 대하여 목표수익율 알림을 전송한다")
	@Test
	void notifyTargetGainAll() {
		// given
		Member member = memberRepository.save(createMember());
		fcmRepository.saveAll(List.of(createFcmToken("token1", member), createFcmToken("token2", member)));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		Stock stock2 = stockRepository.save(createDongwhaPharmStock());
		PortfolioHolding holding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		PortfolioHolding holding2 = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock2));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(100);
		Money purchasePricePerShare = Money.won(100);
		String memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePricePerShare, memo, holding));

		purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		numShares = Count.from(100);
		purchasePricePerShare = Money.won(60000);
		memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePricePerShare, memo, holding2));

		manager.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 60000L));
		manager.savePrice(KisCurrentPrice.create(stock2.getTickerSymbol(), 60000L));
		given(mockedFirebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));

		// when
		List<NotifyMessageItem> actual = service.notifyTargetGainAll();

		// then
		NotifyMessageItem expected = NotifyMessageItem.portfolioNotifyMessageItem(1L, false, "포트폴리오",
			"내꿈은 워렌버핏의 목표 수익율을 달성했습니다",
			NotificationType.PORTFOLIO_TARGET_GAIN, "1", 1L, "/portfolio/1", "내꿈은 워렌버핏",
			List.of("messageId", "messageId"));
		assertAll(
			() -> assertThat(actual)
				.asList()
				.hasSize(1)
				.containsExactly(expected),
			() -> assertThat(notificationRepository.findAllByMemberId(member.getId())).hasSize(1),
			() -> assertThat(sentManager.hasTargetGainSendHistory(portfolio.getId())).isTrue()
		);
	}
}
