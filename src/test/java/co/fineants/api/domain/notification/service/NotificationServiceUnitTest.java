package co.fineants.api.domain.notification.service;

import static co.fineants.TestDataFactory.*;
import static co.fineants.api.domain.notification.domain.entity.type.NotificationType.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.fcm.domain.entity.FcmToken;
import co.fineants.api.domain.fcm.repository.FcmRepository;
import co.fineants.api.domain.fcm.service.FcmService;
import co.fineants.api.domain.fcm.service.FirebaseMessagingService;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.kis.repository.CurrentPriceRepository;
import co.fineants.api.domain.kis.service.CurrentPriceService;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.api.domain.notification.config.NotificationConfig;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessageItem;
import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.domain.entity.policy.MaxLossNotificationPolicy;
import co.fineants.api.domain.notification.domain.entity.policy.TargetGainNotificationPolicy;
import co.fineants.api.domain.notification.domain.entity.policy.TargetPriceNotificationPolicy;
import co.fineants.api.domain.notification.domain.entity.type.NotificationType;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.domain.notification.repository.NotificationSentRepository;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
import co.fineants.api.domain.stock_target_price.domain.entity.TargetPriceNotification;
import co.fineants.api.domain.stock_target_price.repository.StockTargetPriceRepository;
import co.fineants.api.domain.stock_target_price.repository.TargetPriceNotificationRepository;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.member.domain.NotificationPreference;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockRepository;
import co.fineants.stock.domain.calculator.DividendCalculator;

@ExtendWith(MockitoExtension.class)
class NotificationServiceUnitTest {

	private NotificationService service;

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private DividendCalculator dividendCalculator;

	@Mock
	private LocalDateTimeService localDateTimeService;

	@Mock
	private PortfolioRepository portfolioRepository;

	private FcmRepository fcmRepository;

	@Mock
	private StockTargetPriceRepository stockTargetPriceRepository;

	@Mock
	private FcmService fcmService;

	private TargetPriceNotificationRepository targetPriceNotificationRepository;

	private StockRepository stockRepository;

	private PortfolioHoldingRepository portfolioHoldingRepository;

	private PurchaseHistoryRepository purchaseHistoryRepository;

	private FirebaseMessaging firebaseMessaging;

	private CurrentPriceRepository currentPriceRepository;

	@Mock
	private KisService mockedKisService;

	@Mock
	private FirebaseMessagingService firebaseMessagingService;

	@Mock
	private CurrentPriceService currentPriceService;
	@Mock
	private NotificationSentRepository notificationSentRepository;

	@BeforeEach
	void setUp() {
		NotifiableFactory notifiableFactory = new NotifiableFactory(portfolioRepository, stockTargetPriceRepository,
			currentPriceService);
		PortfolioCalculator portfolioCalculator = new PortfolioCalculator(currentPriceService, localDateTimeService,
			dividendCalculator);
		NotifyMessageFactory notifyMessageFactory = new NotifyMessageFactory(fcmService);

		NotificationConfig notificationConfig = new NotificationConfig(notificationSentRepository);
		TargetGainNotificationPolicy targetGainNotificationPolicy = notificationConfig.targetGainNotificationPolicy();
		TargetGainNotificationStrategy targetGainNotificationStrategy = new TargetGainNotificationStrategy(
			targetGainNotificationPolicy, notificationSentRepository);

		MaxLossNotificationPolicy maxLossNotificationPolicy = notificationConfig.maxLossNotificationPolicy();
		MaximumLossNotificationStrategy maximumLossNotificationStrategy = new MaximumLossNotificationStrategy(
			maxLossNotificationPolicy, notificationSentRepository);

		TargetPriceNotificationPolicy targetPriceNotificationPolicy = notificationConfig.targetPriceNotificationPolicy();
		TargetPriceNotificationStrategy targetPriceNotificationStrategy = new TargetPriceNotificationStrategy(
			targetPriceNotificationPolicy, notificationSentRepository
		);

		NotificationSender notificationSender = new NotificationSender(firebaseMessagingService, fcmService);
		service = new NotificationService(
			notificationRepository,
			memberRepository,
			notifyMessageFactory,
			notificationSender,
			targetGainNotificationStrategy,
			maximumLossNotificationStrategy,
			targetPriceNotificationStrategy,
			notifiableFactory,
			portfolioCalculator
		);
	}

	@DisplayName("포트폴리오 목표 수익률 달성 알림 메시지들을 푸시합니다")
	@Test
	void should_save_and_notify_notification_data_when_type_is_target_gain() {
		// given
		Member member = TestDataFactory.createMember(1L);
		Portfolio portfolio = TestDataFactory.createPortfolio(1L, member);
		Stock stock = TestDataFactory.createSamsungStock();
		PortfolioHolding holding = TestDataFactory.createPortfolioHolding(1L, portfolio, stock);

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(100);
		Money purchasePricePerShare = Money.won(10000);
		String memo = "첫구매";
		PurchaseHistory history = createPurchaseHistory(1L, purchaseDate, numShares,
			purchasePricePerShare, memo, holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		FcmToken fcmToken = TestDataFactory.createFcmToken(1L, "fcmToken", member);

		given(firebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("projects/fineants-404407/messages/4754d355-5d5d-4f14-a642-75fecdb91fa5"));
		given(currentPriceService.fetchPrice(stock.getTickerSymbol()))
			.willReturn(Money.won(50_000L));
		given(portfolioRepository.findByPortfolioIdWithAll(portfolio.getId()))
			.willReturn(Optional.of(portfolio));
		given(fcmService.findTokens(member.getId()))
			.willReturn(List.of(fcmToken.getToken()));
		given(memberRepository.findById(member.getId()))
			.willReturn(Optional.of(member));
		Notification notification = Notification.portfolioNotification(
			"포트폴리오",
			PORTFOLIO_TARGET_GAIN,
			portfolio.getReferenceId(),
			portfolio.getLink(),
			member,
			List.of("projects/fineants-404407/messages/4754d355-5d5d-4f14-a642-75fecdb91fa5"),
			portfolio.name(),
			1L
		).withId(1L);
		List<Notification> notifications = List.of(notification);
		given(notificationRepository.saveAll(ArgumentMatchers.anyList()))
			.willReturn(notifications);

		// when
		List<NotifyMessageItem> actual = service.notifyTargetGain(portfolio.getId());

		// then
		NotifyMessageItem expectedItem = NotifyMessageItem.portfolioNotifyMessageItem(
			1L,
			false,
			"포트폴리오",
			portfolio.name() + "의 목표 수익율을 달성했습니다",
			PORTFOLIO_TARGET_GAIN,
			portfolio.getReferenceId(),
			member.getId(),
			portfolio.getLink(),
			portfolio.name(),
			List.of("projects/fineants-404407/messages/4754d355-5d5d-4f14-a642-75fecdb91fa5")
		);
		assertThat(actual)
			.hasSize(1)
			.containsExactly(expectedItem);
		BDDMockito.verify(firebaseMessagingService, times(1))
			.send(ArgumentMatchers.any(Message.class));
		BDDMockito.verify(notificationSentRepository, times(1))
			.addTargetGainSendHistory(notification);
		BDDMockito.verify(notificationRepository, times(1))
			.saveAll(anyList());
	}

	@DisplayName("목표수익률에 도달하지 않아서 알림을 보내지 않는다")
	@Test
	void should_return_empty_list_when_not_reached_target_gain_amount() {
		// given
		Member member = TestDataFactory.createMember(1L);
		Portfolio portfolio = TestDataFactory.createPortfolio(1L, member);
		Stock samsung = TestDataFactory.createSamsungStock();
		Stock ccs = TestDataFactory.createCcsStack();

		PortfolioHolding holding = TestDataFactory.createPortfolioHolding(1L, portfolio, samsung);
		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(12);
		Money purchasePricePerShare = Money.won(60000);
		String memo = "첫구매";
		PurchaseHistory history = createPurchaseHistory(1L, purchaseDate, numShares, purchasePricePerShare, memo,
			holding);
		holding.addPurchaseHistory(history);

		PortfolioHolding holding2 = TestDataFactory.createPortfolioHolding(2L, portfolio, ccs);
		purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		numShares = Count.from(15);
		purchasePricePerShare = Money.won(2000);
		memo = "첫구매";
		PurchaseHistory history2 = createPurchaseHistory(2L, purchaseDate, numShares, purchasePricePerShare, memo,
			holding2);
		holding2.addPurchaseHistory(history2);

		given(portfolioRepository.findByPortfolioIdWithAll(portfolio.getId()))
			.willReturn(Optional.of(portfolio));

		// when
		List<NotifyMessageItem> actual = service.notifyTargetGain(portfolio.getId());

		// then
		assertThat(actual).isEmpty();
	}

	@DisplayName("토큰이 유효하지 않아서 목표 수익률 알림을 보낼수 없지만, 알림은 저장된다")
	@Test
	void should_save_notification_to_db_when_invalid_fcm_token_then_can_not_send_target_gain_notification() {
		// given
		Member member = TestDataFactory.createMember(1L);
		Portfolio portfolio = TestDataFactory.createPortfolio(1L, member);
		Stock stock = TestDataFactory.createSamsungStock();

		PortfolioHolding holding = TestDataFactory.createPortfolioHolding(1L, portfolio, stock);
		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(100);
		Money purchasePricePerShare = Money.won(10000);
		String memo = "첫구매";
		PurchaseHistory history = createPurchaseHistory(1L, purchaseDate, numShares, purchasePricePerShare, memo,
			holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		given(firebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.empty());
		given(currentPriceService.fetchPrice(stock.getTickerSymbol()))
			.willReturn(Money.won(50_000L));
		given(portfolioRepository.findByPortfolioIdWithAll(portfolio.getId()))
			.willReturn(Optional.of(portfolio));
		given(fcmService.findTokens(member.getId()))
			.willReturn(List.of("fcmToken"));
		given(memberRepository.findById(member.getId()))
			.willReturn(Optional.of(member));
		Notification notification = Notification.portfolioNotification("포트폴리오", PORTFOLIO_TARGET_GAIN,
			portfolio.getReferenceId(), portfolio.getLink(), member, List.of(""),
			portfolio.name(), portfolio.getId()).withId(1L);
		List<Notification> notifications = List.of(notification);
		given(notificationRepository.saveAll(anyList()))
			.willReturn(notifications);

		// when
		List<NotifyMessageItem> actual = service.notifyTargetGain(portfolio.getId());

		// then
		Assertions.assertThat(actual)
			.hasSize(1);
		verify(fcmService, times(1))
			.deleteToken("fcmToken");
		verify(notificationSentRepository, times(1))
			.addTargetGainSendHistory(any(Notification.class));
	}

	@DisplayName("브라우저 알림 설정이 비활성화되어 목표 수익률 알림을 보낼수 없다")
	@CsvSource(value = {"false,true", "true,false", "false, false"})
	@ParameterizedTest
	void should_can_not_send_target_gain_notification_when_preference_is_inactive_then_return_empty_list(
		boolean browserNotify, boolean targetGainNotify) {
		// given
		Member member = TestDataFactory.createMember(1L);
		NotificationPreference changePreference = TestDataFactory.createNotificationPreference(browserNotify,
			targetGainNotify, true, true);
		member.setNotificationPreference(changePreference);
		Portfolio portfolio = TestDataFactory.createPortfolio(1L, member);
		Stock stock = TestDataFactory.createSamsungStock();
		PortfolioHolding holding = TestDataFactory.createPortfolioHolding(1L, portfolio, stock);

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(100);
		Money purchasePricePerShare = Money.won(10000);
		String memo = "첫구매";
		PurchaseHistory history = createPurchaseHistory(1L, purchaseDate, numShares, purchasePricePerShare, memo,
			holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		given(currentPriceService.fetchPrice(stock.getTickerSymbol()))
			.willReturn(Money.won(50_000L));
		given(portfolioRepository.findByPortfolioIdWithAll(portfolio.getId()))
			.willReturn(Optional.of(portfolio));

		// when
		List<NotifyMessageItem> actual = service.notifyTargetGain(portfolio.getId());

		// then
		assertThat(actual).isEmpty();
	}

	@DisplayName("모든 포트폴리오의 최대 손실율 도달을 만족하는 회원들에게 알림을 푸시한다")
	@Test
	void should_send_max_loss_notification_and_save_notification_when_reached_maximum_loss_amount() {
		// given
		Member member = TestDataFactory.createMember(1L);
		Portfolio portfolio = TestDataFactory.createPortfolio(1L, member);
		Stock stock = TestDataFactory.createSamsungStock();
		PortfolioHolding holding = TestDataFactory.createPortfolioHolding(1L, portfolio, stock);

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(50);
		Money purchasePricePerShare = Money.won(60000);
		String memo = "첫구매";
		PurchaseHistory history = createPurchaseHistory(1L, purchaseDate, numShares, purchasePricePerShare, memo,
			holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		given(portfolioRepository.findAllWithAll())
			.willReturn(List.of(portfolio));
		given(memberRepository.findById(member.getId()))
			.willReturn(Optional.of(member));
		given(fcmService.findTokens(member.getId()))
			.willReturn(List.of("token"));

		given(firebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));
		given(currentPriceService.fetchPrice(stock.getTickerSymbol()))
			.willReturn(Money.won(100L));
		Notification notification = Notification.portfolioNotification(
			"포트폴리오",
			PORTFOLIO_MAX_LOSS,
			portfolio.getReferenceId(),
			portfolio.getLink(),
			member,
			List.of("projects/fineants-404407/messages/4754d355-5d5d-4f14-a642-75fecdb91fa5"),
			portfolio.name(),
			1L
		).withId(1L);
		given(notificationRepository.saveAll(anyList()))
			.willReturn(List.of(notification));

		// when
		List<NotifyMessageItem> actual = service.notifyMaxLossAll();

		// then
		assertThat(actual).hasSize(1);
		// 토큰 삭제되지 않는것 검증
		verify(fcmService, times(0))
			.deleteToken("token");

		// 알림 저장 검증
		verify(notificationRepository, times(1))
			.saveAll(anyList());

		// 알림 전송 검증
		verify(notificationSentRepository, times(1))
			.addMaxLossSendHistory(notification);
	}

	@DisplayName("포트폴리오의 최대 손실율에 도달하여 사용자에게 알림을 푸시합니다")
	@Test
	void should_send_max_loss_notification_save_notification_when_reached_max_loss_amount() {
		// given
		Member member = TestDataFactory.createMember(1L);
		Portfolio portfolio = TestDataFactory.createPortfolio(1L, member);
		Stock stock = TestDataFactory.createSamsungStock();
		PortfolioHolding holding = TestDataFactory.createPortfolioHolding(1L, portfolio, stock);

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(50);
		Money purchasePricePerShare = Money.won(60000);
		String memo = "첫구매";
		PurchaseHistory history = createPurchaseHistory(1L, purchaseDate, numShares, purchasePricePerShare, memo,
			holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		given(fcmService.findTokens(member.getId()))
			.willReturn(List.of("token"));

		given(firebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));
		given(currentPriceService.fetchPrice(stock.getTickerSymbol()))
			.willReturn(Money.won(100L));
		given(portfolioRepository.findByPortfolioIdWithAll(portfolio.getId()))
			.willReturn(Optional.of(portfolio));
		given(memberRepository.findById(member.getId()))
			.willReturn(Optional.of(member));
		Notification notification = Notification.portfolioNotification(
			"포트폴리오",
			PORTFOLIO_MAX_LOSS,
			portfolio.getReferenceId(),
			portfolio.getLink(),
			member,
			List.of("projects/fineants-404407/messages/4754d355-5d5d-4f14-a642-75fecdb91fa5"),
			portfolio.name(),
			1L
		).withId(1L);
		given(notificationRepository.saveAll(anyList()))
			.willReturn(List.of(notification));

		// when
		List<NotifyMessageItem> actual = service.notifyMaxLoss(portfolio.getId());

		// then
		assertThat(actual).hasSize(1);
		// 토큰 삭제되지 않는것 검증
		verify(fcmService, times(0))
			.deleteToken("token");

		// 알림 저장 검증
		verify(notificationRepository, times(1))
			.saveAll(anyList());

		// 알림 전송 검증
		verify(notificationSentRepository, times(1))
			.addMaxLossSendHistory(notification);
	}

	@DisplayName("알림 설정이 비활성화 되어 있어서 포트폴리오의 최대 손실율에 도달하여 사용자에게 알림을 푸시할 수 없습니다")
	@CsvSource(value = {"false,true", "true,false", "false, false"})
	@ParameterizedTest
	void should_not_send_max_loss_notification_when_preference_is_inactive(boolean browserNotify,
		boolean maxLossNotify) {
		// given
		Member member = TestDataFactory.createMember(1L);
		NotificationPreference changePreference = TestDataFactory.createNotificationPreference(browserNotify, true,
			maxLossNotify,
			true);
		member.setNotificationPreference(changePreference);
		Portfolio portfolio = TestDataFactory.createPortfolio(1L, member);
		Stock stock = TestDataFactory.createSamsungStock();
		PortfolioHolding holding = TestDataFactory.createPortfolioHolding(1L, portfolio, stock);

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(50);
		Money purchasePricePerShare = Money.won(60000);
		String memo = "첫구매";
		PurchaseHistory history = createPurchaseHistory(1L, purchaseDate, numShares, purchasePricePerShare, memo,
			holding);
		holding.addPurchaseHistory(history);
		portfolio.addHolding(holding);

		given(currentPriceService.fetchPrice(stock.getTickerSymbol()))
			.willReturn(Money.won(50_000L));
		given(portfolioRepository.findByPortfolioIdWithAll(portfolio.getId()))
			.willReturn(Optional.of(portfolio));

		// when
		List<NotifyMessageItem> actual = service.notifyMaxLoss(portfolio.getId());

		// then
		assertThat(actual).isEmpty();
	}

	@DisplayName("토큰이 유효하지 않아서 최대 손실율 달성 알림을 보낼수 없지만, 알림은 저장된다")
	@Test
	void should_save_max_loss_notification_when_invalid_fcm_token_then_delete_fcm_token() {
		// given
		Member member = TestDataFactory.createMember(1L);
		Portfolio portfolio = TestDataFactory.createPortfolio(1L, member);
		Stock stock = TestDataFactory.createSamsungStock();
		PortfolioHolding portfolioHolding = TestDataFactory.createPortfolioHolding(1L, portfolio, stock);
		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(10);
		Money purchasePricePerShare = Money.won(60000);
		String memo = "첫구매";
		PurchaseHistory history = createPurchaseHistory(1L, purchaseDate, numShares, purchasePricePerShare, memo,
			portfolioHolding);
		portfolioHolding.addPurchaseHistory(history);
		portfolio.addHolding(portfolioHolding);

		given(fcmService.findTokens(member.getId()))
			.willReturn(List.of("fcmToken"));

		given(firebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.empty());
		given(currentPriceService.fetchPrice(stock.getTickerSymbol()))
			.willReturn(Money.won(50_000L));
		given(portfolioRepository.findByPortfolioIdWithAll(portfolio.getId()))
			.willReturn(Optional.of(portfolio));
		given(memberRepository.findById(member.getId()))
			.willReturn(Optional.of(member));
		Notification notification = Notification.portfolioNotification(
			"포트폴리오",
			PORTFOLIO_TARGET_GAIN,
			portfolio.getReferenceId(),
			portfolio.getLink(),
			member,
			List.of("projects/fineants-404407/messages/4754d355-5d5d-4f14-a642-75fecdb91fa5"),
			portfolio.name(),
			1L
		).withId(1L);
		List<Notification> notifications = List.of(notification);
		given(notificationRepository.saveAll(ArgumentMatchers.anyList()))
			.willReturn(notifications);

		// when
		List<NotifyMessageItem> actual = service.notifyMaxLoss(portfolio.getId());

		// then
		assertThat(actual).hasSize(1);
		verify(fcmService, times(1))
			.deleteToken("fcmToken");
		verify(notificationRepository, times(1))
			.saveAll(anyList());
		verify(notificationSentRepository, times(1))
			.addMaxLossSendHistory(notification);
	}

	@DisplayName("종목의 현재가가 변경됨에 따라 포트폴리오의 목표 수익률을 달성하여 사용자에게 알림을 전송한다")
	@Test
	void when_current_price_changed_and_reached_target_gain_then_send_notification_and_save_notification() {
		// given
		Member member = TestDataFactory.createMember(1L);
		Portfolio portfolio = TestDataFactory.createPortfolio(1L, member);
		Stock stock = TestDataFactory.createSamsungStock();
		Stock stock2 = TestDataFactory.createDongwhaPharmStock();
		PortfolioHolding holding = TestDataFactory.createPortfolioHolding(1L, portfolio, stock);
		PortfolioHolding holding2 = TestDataFactory.createPortfolioHolding(2L, portfolio, stock2);

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(100);
		Money purchasePricePerShare = Money.won(100);
		String memo = "첫구매";
		PurchaseHistory history1 = createPurchaseHistory(1L, purchaseDate, numShares, purchasePricePerShare, memo,
			holding);
		holding.addPurchaseHistory(history1);

		purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		numShares = Count.from(1);
		purchasePricePerShare = Money.won(60000);
		memo = "첫구매";
		PurchaseHistory history2 = createPurchaseHistory(2L, purchaseDate, numShares, purchasePricePerShare, memo,
			holding2);
		holding2.addPurchaseHistory(history2);

		portfolio.addHolding(holding);
		portfolio.addHolding(holding2);

		given(fcmService.findTokens(member.getId()))
			.willReturn(List.of("token1", "token2"));
		given(currentPriceService.fetchPrice(stock.getTickerSymbol()))
			.willReturn(Money.won(60_000));
		given(currentPriceService.fetchPrice(stock2.getTickerSymbol()))
			.willReturn(Money.won(60_000L));
		given(firebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));
		given(portfolioRepository.findAllWithAll())
			.willReturn(List.of(portfolio));
		given(memberRepository.findById(member.getId()))
			.willReturn(Optional.of(member));
		Notification notification = Notification.portfolioNotification(
			"포트폴리오",
			PORTFOLIO_TARGET_GAIN,
			portfolio.getReferenceId(),
			portfolio.getLink(),
			member,
			List.of("projects/fineants-404407/messages/4754d355-5d5d-4f14-a642-75fecdb91fa5"),
			portfolio.name(),
			1L
		).withId(1L);
		given(notificationRepository.saveAll(anyList()))
			.willReturn(List.of(notification));

		// when
		List<NotifyMessageItem> items = service.notifyTargetGainAll();

		// then
		assertThat(items).hasSize(1);
		// 토큰 삭제되지 않는것 검증
		verify(fcmService, times(0))
			.deleteToken("token1");
		verify(fcmService, times(0))
			.deleteToken("token2");

		// 알림 저장 검증
		verify(notificationRepository, times(1))
			.saveAll(anyList());

		// 알림 전송 검증
		verify(notificationSentRepository, times(1))
			.addTargetGainSendHistory(notification);
	}

	@DisplayName("모든 회원들을 대상으로 특정 티커 심볼에 대한 종목 지정가 알림을 발송한다")
	@Test
	void should_send_target_price_notification_when_current_price_is_reached_price() {
		// given
		Member member = TestDataFactory.createMember(1L);
		Member member2 = TestDataFactory.createMember(2L);

		Stock stock = createSamsungStock();
		Stock stock2 = createDongwhaPharmStock();

		StockTargetPrice stockTargetPrice1 = TestDataFactory.createStockTargetPrice(1L, member, stock);
		StockTargetPrice stockTargetPrice2 = TestDataFactory.createStockTargetPrice(2L, member, stock2);

		List<TargetPriceNotification> targetPriceNotification = createTargetPriceNotification(List.of(1L, 2L),
			stockTargetPrice1, List.of(60000L, 70000L));
		List<TargetPriceNotification> targetPriceNotification2 = createTargetPriceNotification(List.of(3L, 4L),
			stockTargetPrice2, List.of(10000L, 20000L));
		targetPriceNotification.forEach(stockTargetPrice1::addTargetPriceNotification);
		targetPriceNotification2.forEach(stockTargetPrice2::addTargetPriceNotification);

		StockTargetPrice stockTargetPrice3 = createStockTargetPrice(member2, stock);
		StockTargetPrice stockTargetPrice4 = createStockTargetPrice(member2, stock2);

		List<TargetPriceNotification> targetPriceNotification3 = createTargetPriceNotification(List.of(5L, 6L),
			stockTargetPrice3, List.of(60000L, 70000L));
		List<TargetPriceNotification> targetPriceNotification4 = createTargetPriceNotification(List.of(7L, 8L),
			stockTargetPrice4, List.of(10000L, 20000L));
		targetPriceNotification3.forEach(stockTargetPrice3::addTargetPriceNotification);
		targetPriceNotification4.forEach(stockTargetPrice4::addTargetPriceNotification);

		given(currentPriceService.fetchPrice(stock.getTickerSymbol()))
			.willReturn(Money.won(60_000));
		given(currentPriceService.fetchPrice(stock2.getTickerSymbol()))
			.willReturn(Money.won(10_000));

		given(firebaseMessagingService.send(any(Message.class)))
			.willReturn(Optional.of("messageId"));

		List<String> tickerSymbols = Stream.of(stock, stock2)
			.map(Stock::getTickerSymbol)
			.toList();

		given(fcmService.findTokens(member.getId()))
			.willReturn(List.of("token1"));
		given(fcmService.findTokens(member2.getId()))
			.willReturn(List.of("token2"));
		given(stockTargetPriceRepository.findAllByTickerSymbols(
			tickerSymbols))
			.willReturn(List.of(stockTargetPrice1, stockTargetPrice2, stockTargetPrice3, stockTargetPrice4));
		given(memberRepository.findById(member.getId()))
			.willReturn(Optional.of(member));
		given(memberRepository.findById(member2.getId()))
			.willReturn(Optional.of(member2));
		List<Notification> notifications = getMockNotification(stockTargetPrice1, member, stock, stockTargetPrice3,
			stock2, stockTargetPrice4);
		given(notificationRepository.saveAll(anyList()))
			.willReturn(notifications);

		// when
		List<NotifyMessageItem> actual = service.notifyTargetPriceBy(tickerSymbols);

		// then
		assertThat(actual).hasSize(4);
		verify(notificationRepository, times(1))
			.saveAll(anyList());
		verify(fcmService, times(0))
			.deleteToken(anyString());
		verify(notificationSentRepository, times(4))
			.addTargetPriceSendHistory(ArgumentMatchers.any(Notification.class));
	}

	private List<Notification> getMockNotification(StockTargetPrice stockTargetPrice1, Member member, Stock stock,
		StockTargetPrice stockTargetPrice3, Stock stock2, StockTargetPrice stockTargetPrice4) {
		return List.of(
			Notification.stockTargetPriceNotification(
				"종목 지정가",
				stockTargetPrice1.getReferenceId(),
				stockTargetPrice1.getLink(),
				member,
				List.of("messageId"),
				stock.getCompanyName(),
				Money.won(60_000L),
				1L
			).withId(1L),
			Notification.stockTargetPriceNotification(
				"종목 지정가",
				stockTargetPrice3.getReferenceId(),
				stockTargetPrice3.getLink(),
				member,
				List.of("messageId"),
				stock.getCompanyName(),
				Money.won(60_000L),
				5L
			).withId(2L),
			Notification.stockTargetPriceNotification(
				"종목 지정가",
				stockTargetPrice3.getReferenceId(),
				stockTargetPrice3.getLink(),
				member,
				List.of("messageId"),
				stock2.getCompanyName(),
				Money.won(10_000L),
				3L
			).withId(3L),
			Notification.stockTargetPriceNotification(
				"종목 지정가",
				stockTargetPrice4.getReferenceId(),
				stockTargetPrice4.getLink(),
				member,
				List.of("messageId"),
				stock2.getCompanyName(),
				Money.won(10_000L),
				7L
			).withId(4L)
		);
	}

	@DisplayName("종목 지정가 알림 발송 시나리오")
	@TestFactory
	Collection<DynamicTest> createNotifyTargetPriceDynamicTest() {
		return List.of(
			DynamicTest.dynamicTest("종목 지정가 알림을 전송한다", () -> {
				// given
				Member member = TestDataFactory.createMember(1L, "네모네모", "dragonbead95@naver.com");
				FcmToken fcmToken = createFcmToken("token1", member);
				Stock stock = TestDataFactory.createSamsungStock();

				StockTargetPrice stockTargetPrice1 = createStockTargetPrice(member, stock);
				List<TargetPriceNotification> targetPriceNotifications = createTargetPriceNotification(List.of(1L, 2L),
					stockTargetPrice1, List.of(60000L, 70000L));
				targetPriceNotifications.forEach(stockTargetPrice1::addTargetPriceNotification);

				List<String> tickerSymbols = Stream.of(stock)
					.map(Stock::getTickerSymbol)
					.toList();

				BDDMockito.given(stockTargetPriceRepository.findAllByTickerSymbols(tickerSymbols))
					.willReturn(List.of(stockTargetPrice1));
				BDDMockito.given(fcmService.findTokens(member.getId()))
					.willReturn(List.of(fcmToken.getToken()));
				BDDMockito.given(notificationSentRepository.hasTargetPriceSendHistory(1L))
					.willReturn(false);
				BDDMockito.given(memberRepository.findById(member.getId()))
					.willReturn(Optional.of(member));
				Notification notification = Notification.stockTargetPriceNotification(
					"종목 지정가",
					stockTargetPrice1.getReferenceId(),
					stockTargetPrice1.getLink(),
					member,
					List.of("messageId"),
					stock.getCompanyName(),
					Money.won(60_000L),
					1L
				).withId(1L);
				BDDMockito.given(notificationRepository.saveAll(ArgumentMatchers.anyList()))
					.willReturn(List.of(notification));
				BDDMockito.given(currentPriceService.fetchPrice(stock.getTickerSymbol()))
					.willReturn(Money.won(60_000L));
				BDDMockito.given(firebaseMessagingService.send(any(Message.class)))
					.willReturn(Optional.of("messageId"));
				// when
				List<NotifyMessageItem> actual = service.notifyTargetPriceBy(tickerSymbols);

				// then
				NotifyMessageItem expected1 = NotifyMessageItem.targetPriceNotifyMessageItem(
					notification.getId(),
					false,
					"종목 지정가",
					"삼성전자보통주이(가) ₩60,000에 도달했습니다",
					NotificationType.STOCK_TARGET_PRICE,
					"005930",
					member.getId(),
					"/stock/005930",
					List.of("messageId"),
					"삼성전자보통주",
					Money.won(60000),
					targetPriceNotifications.get(0).getId()
				);
				Assertions.assertThat(actual).hasSize(1)
					.containsExactly(expected1);
				BDDMockito.verify(notificationSentRepository, times(1))
					.addTargetPriceSendHistory(notification);
			}),
			DynamicTest.dynamicTest("전송 이력이 있어서 알림을 받지 않는다", () -> {
				// given
				Stock stock = createSamsungStock();
				List<String> tickerSymbols = Stream.of(stock)
					.map(Stock::getTickerSymbol)
					.toList();
				BDDMockito.given(notificationSentRepository.hasTargetPriceSendHistory(1L))
					.willReturn(true);
				BDDMockito.given(notificationRepository.saveAll(ArgumentMatchers.anyList()))
					.willReturn(Collections.emptyList());

				// when
				List<NotifyMessageItem> actual = service.notifyTargetPriceBy(tickerSymbols);

				// then
				Assertions.assertThat(actual).isEmpty();
				BDDMockito.verify(notificationSentRepository, times(2))
					.hasTargetPriceSendHistory(1L);
			})
		);
	}

	// @DisplayName("사용자는 사용자가 지정한 종목 지정가에 대한 푸시 알림을 받는다")
	// @Test
	// void sendStockTargetPriceNotification() {
	// 	// given
	// 	Member member = memberRepository.save(createMember());
	// 	fcmRepository.save(createFcmToken("token", member));
	// 	fcmRepository.save(createFcmToken("token2", member));
	// 	Stock stock = stockRepository.save(createSamsungStock());
	// 	Stock stock2 = stockRepository.save(createDongwhaPharmStock());
	// 	StockTargetPrice stockTargetPrice = stockTargetPriceRepository.save(createStockTargetPrice(member, stock));
	// 	StockTargetPrice stockTargetPrice2 = stockTargetPriceRepository.save(createStockTargetPrice(member, stock2));
	// 	List<TargetPriceNotification> targetPriceNotifications = targetPriceNotificationRepository.saveAll(
	// 		createTargetPriceNotification(stockTargetPrice, List.of(60000L, 70000L)));
	// 	List<TargetPriceNotification> targetPriceNotifications2 = targetPriceNotificationRepository.saveAll(
	// 		createTargetPriceNotification(stockTargetPrice2, List.of(10000L, 20000L)));
	//
	// 	currentPriceRepository.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 60000L));
	// 	currentPriceRepository.savePrice(KisCurrentPrice.create(stock2.getTickerSymbol(), 10000L));
	// 	given(mockedFirebaseMessagingService.send(any(Message.class)))
	// 		.willReturn(Optional.of("messageId"));
	// 	// when
	// 	List<NotifyMessageItem> actual = service.notifyTargetPrice(member.getId());
	//
	// 	// then
	// 	NotifyMessageItem expected1 = NotifyMessageItem.targetPriceNotifyMessageItem(
	// 		1L,
	// 		false,
	// 		"종목 지정가",
	// 		"삼성전자보통주이(가) ₩60,000에 도달했습니다",
	// 		NotificationType.STOCK_TARGET_PRICE,
	// 		"005930",
	// 		member.getId(),
	// 		"/stock/005930",
	// 		List.of("messageId", "messageId"),
	// 		"삼성전자보통주",
	// 		Money.won(60000),
	// 		targetPriceNotifications.get(0).getId()
	// 	);
	// 	NotifyMessageItem expected2 = NotifyMessageItem.targetPriceNotifyMessageItem(
	// 		2L,
	// 		false,
	// 		"종목 지정가",
	// 		"동화약품보통주이(가) ₩10,000에 도달했습니다",
	// 		NotificationType.STOCK_TARGET_PRICE,
	// 		"000020",
	// 		member.getId(),
	// 		"/stock/000020",
	// 		List.of("messageId", "messageId"),
	// 		"동화약품보통주",
	// 		Money.won(10000),
	// 		targetPriceNotifications2.get(0).getId()
	// 	);
	//
	// 	assertThat(actual)
	// 		.hasSize(2)
	// 		.usingComparatorForType(Money::compareTo, Money.class)
	// 		.containsExactly(expected1, expected2);
	// 	assertThat(notificationRepository.findAllByMemberId(member.getId()))
	// 		.asList()
	// 		.hasSize(2);
	// }
	//
	// @DisplayName("사용자는 종목 지정가 도달 알림을 받은 상태에서 추가적인 종목 지정가 도달을 하면 알림을 보내지 않는다")
	// @Test
	// void notifyTargetPrice_whenExistNotification_thenNotSentNotification() {
	// 	// given
	// 	Member member = memberRepository.save(createMember());
	// 	fcmRepository.save(createFcmToken("token", member));
	// 	Stock stock = stockRepository.save(createSamsungStock());
	// 	Stock stock2 = stockRepository.save(createDongwhaPharmStock());
	// 	StockTargetPrice stockTargetPrice = stockTargetPriceRepository.save(createStockTargetPrice(member, stock));
	// 	StockTargetPrice stockTargetPrice2 = stockTargetPriceRepository.save(createStockTargetPrice(member, stock2));
	// 	List<TargetPriceNotification> targetPriceNotifications = createTargetPriceNotification(stockTargetPrice,
	// 		List.of(60000L, 70000L));
	// 	List<TargetPriceNotification> targetPriceNotifications2 = createTargetPriceNotification(stockTargetPrice2,
	// 		List.of(10000L, 20000L));
	// 	targetPriceNotificationRepository.saveAll(targetPriceNotifications);
	// 	targetPriceNotificationRepository.saveAll(targetPriceNotifications2);
	//
	// 	TargetPriceNotification sendTargetPriceNotification = targetPriceNotifications.get(0);
	// 	Notification notification = notificationRepository.save(Notification.stockTargetPriceNotification(
	// 		"종목 지정가", sendTargetPriceNotification.getStockTargetPrice().getStock().getTickerSymbol(),
	// 		"/stock/" + sendTargetPriceNotification.getStockTargetPrice().getStock().getTickerSymbol(), member,
	// 		List.of("messageId"), sendTargetPriceNotification.getStockTargetPrice().getStock().getTickerSymbol(),
	// 		sendTargetPriceNotification.getTargetPrice(),
	// 		sendTargetPriceNotification.getId()
	// 	));
	//
	// 	currentPriceRepository.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 60000L));
	// 	currentPriceRepository.savePrice(KisCurrentPrice.create(stock2.getTickerSymbol(), 10000L));
	// 	sentManager.addTargetPriceSendHistory(notification);
	// 	given(mockedFirebaseMessagingService.send(any(Message.class)))
	// 		.willReturn(Optional.of("messageId"));
	// 	// when
	// 	List<NotifyMessageItem> actual = service.notifyTargetPriceBy(
	// 		List.of(stock.getTickerSymbol(), stock2.getTickerSymbol()));
	//
	// 	// then
	// 	NotifyMessageItem expected1 = NotifyMessageItem.targetPriceNotifyMessageItem(
	// 		2L,
	// 		false,
	// 		"종목 지정가",
	// 		"동화약품보통주이(가) ₩10,000에 도달했습니다",
	// 		NotificationType.STOCK_TARGET_PRICE,
	// 		"000020",
	// 		member.getId(),
	// 		"/stock/000020",
	// 		List.of("messageId"),
	// 		"동화약품보통주",
	// 		Money.won(10_000),
	// 		targetPriceNotifications2.get(0).getId()
	// 	);
	// 	assertThat(actual)
	// 		.hasSize(1)
	// 		.containsExactly(expected1);
	// 	assertThat(notificationRepository.findAllByMemberId(member.getId()))
	// 		.asList()
	// 		.hasSize(2);
	// }
	//
	// @DisplayName("종목 지정가 도달 알림을 보내는데 실패해도 알림은 저장되어야 한다")
	// @Test
	// void notifyTargetPrice_whenFailSendingNotification_thenSaveNotification() {
	// 	// given
	// 	Member member = memberRepository.save(createMember());
	// 	fcmRepository.save(createFcmToken("token", member));
	// 	Stock stock = stockRepository.save(createSamsungStock());
	// 	StockTargetPrice stockTargetPrice = stockTargetPriceRepository.save(createStockTargetPrice(member, stock));
	// 	List<TargetPriceNotification> targetPriceNotifications = createTargetPriceNotification(stockTargetPrice,
	// 		List.of(60000L, 70000L));
	// 	targetPriceNotificationRepository.saveAll(targetPriceNotifications);
	//
	// 	currentPriceRepository.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 60000L));
	// 	given(mockedFirebaseMessagingService.send(any(Message.class)))
	// 		.willReturn(Optional.empty());
	// 	// when
	// 	List<NotifyMessageItem> actual = service.notifyTargetPriceBy(List.of(stock.getTickerSymbol()));
	//
	// 	// then
	// 	assertThat(actual)
	// 		.asList()
	// 		.hasSize(1);
	// 	assertThat(notificationRepository.findAllByMemberId(member.getId()))
	// 		.asList()
	// 		.hasSize(1);
	// }
	//
	// @DisplayName("티커 심볼을 기준으로 종목 지정가 알림을 발송한다")
	// @Test
	// void notifyTargetPrice_whenMultipleMember_thenSendNotification() {
	// 	// given
	// 	Member member = memberRepository.save(createMember());
	// 	fcmRepository.save(createFcmToken("token", member));
	// 	Stock stock = stockRepository.save(createSamsungStock());
	// 	Stock stock2 = stockRepository.save(createDongwhaPharmStock());
	// 	StockTargetPrice stockTargetPrice = stockTargetPriceRepository.save(createStockTargetPrice(member, stock));
	// 	StockTargetPrice stockTargetPrice2 = stockTargetPriceRepository.save(createStockTargetPrice(member, stock2));
	// 	List<TargetPriceNotification> targetPriceNotifications = targetPriceNotificationRepository.saveAll(
	// 		createTargetPriceNotification(stockTargetPrice, List.of(60000L, 70000L)));
	// 	List<TargetPriceNotification> targetPriceNotifications2 = targetPriceNotificationRepository.saveAll(
	// 		createTargetPriceNotification(stockTargetPrice2, List.of(10000L, 20000L)));
	//
	// 	currentPriceRepository.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 60000L));
	// 	currentPriceRepository.savePrice(KisCurrentPrice.create(stock2.getTickerSymbol(), 10000L));
	// 	given(mockedFirebaseMessagingService.send(any(Message.class)))
	// 		.willReturn(Optional.of("messageId"));
	// 	// when
	// 	List<NotifyMessageItem> actual = service.notifyTargetPriceBy(
	// 		List.of(stock.getTickerSymbol(), stock2.getTickerSymbol()));
	//
	// 	// then
	// 	NotifyMessageItem expected1 = NotifyMessageItem.targetPriceNotifyMessageItem(
	// 		1L,
	// 		false,
	// 		"종목 지정가",
	// 		"삼성전자보통주이(가) ₩60,000에 도달했습니다",
	// 		NotificationType.STOCK_TARGET_PRICE,
	// 		"005930",
	// 		member.getId(),
	// 		"/stock/005930",
	// 		List.of("messageId"),
	// 		"삼성전자보통주",
	// 		Money.won(60_000),
	// 		targetPriceNotifications.get(0).getId()
	// 	);
	// 	NotifyMessageItem expected2 = NotifyMessageItem.targetPriceNotifyMessageItem(
	// 		2L,
	// 		false,
	// 		"종목 지정가",
	// 		"동화약품보통주이(가) ₩10,000에 도달했습니다",
	// 		NotificationType.STOCK_TARGET_PRICE,
	// 		"000020",
	// 		member.getId(),
	// 		"/stock/000020",
	// 		List.of("messageId"),
	// 		"동화약품보통주",
	// 		Money.won(10_000),
	// 		targetPriceNotifications2.get(0).getId()
	// 	);
	// 	assertThat(actual)
	// 		.hasSize(2)
	// 		.containsExactly(expected1, expected2);
	// 	assertThat(notificationRepository.findAllByMemberId(member.getId()))
	// 		.asList()
	// 		.hasSize(2);
	// }
	//
	// @DisplayName("조건을 만족한 포트폴리오에 대하여 목표수익율 알림을 전송한다")
	// @Test
	// void notifyTargetGainAll() {
	// 	// given
	// 	Member member = memberRepository.save(createMember());
	// 	fcmRepository.saveAll(List.of(createFcmToken("token1", member), createFcmToken("token2", member)));
	// 	Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
	// 	Stock stock = stockRepository.save(createSamsungStock());
	// 	Stock stock2 = stockRepository.save(createDongwhaPharmStock());
	// 	PortfolioHolding holding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
	// 	PortfolioHolding holding2 = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock2));
	//
	// 	LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
	// 	Count numShares = Count.from(100);
	// 	Money purchasePricePerShare = Money.won(100);
	// 	String memo = "첫구매";
	// 	purchaseHistoryRepository.save(
	// 		createPurchaseHistory(null, purchaseDate, numShares, purchasePricePerShare, memo, holding));
	//
	// 	purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
	// 	numShares = Count.from(100);
	// 	purchasePricePerShare = Money.won(60000);
	// 	memo = "첫구매";
	// 	purchaseHistoryRepository.save(
	// 		createPurchaseHistory(null, purchaseDate, numShares, purchasePricePerShare, memo, holding2));
	//
	// 	currentPriceRepository.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 60000L));
	// 	currentPriceRepository.savePrice(KisCurrentPrice.create(stock2.getTickerSymbol(), 60000L));
	// 	given(mockedFirebaseMessagingService.send(any(Message.class)))
	// 		.willReturn(Optional.of("messageId"));
	//
	// 	// when
	// 	List<NotifyMessageItem> actual = service.notifyTargetGainAll();
	//
	// 	// then
	// 	NotifyMessageItem expected = NotifyMessageItem.portfolioNotifyMessageItem(1L, false, "포트폴리오",
	// 		"내꿈은 워렌버핏의 목표 수익율을 달성했습니다",
	// 		NotificationType.PORTFOLIO_TARGET_GAIN, "1", 1L, "/portfolio/1", "내꿈은 워렌버핏",
	// 		List.of("messageId", "messageId"));
	// 	assertAll(
	// 		() -> assertThat(actual)
	// 			.asList()
	// 			.hasSize(1)
	// 			.containsExactly(expected),
	// 		() -> assertThat(notificationRepository.findAllByMemberId(member.getId())).hasSize(1),
	// 		() -> assertThat(sentManager.hasTargetGainSendHistory(portfolio.getId())).isTrue()
	// 	);
	// }
}
