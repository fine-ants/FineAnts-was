package co.fineants.api.domain.notification.event.listener;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.fcm.repository.FcmRepository;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessageItem;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.event.aop.PurchaseHistoryEventSendableParameter;
import co.fineants.api.domain.purchasehistory.event.domain.PushNotificationEvent;
import co.fineants.api.domain.purchasehistory.event.listener.PurchaseHistoryEventListener;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;

class PurchaseHistoryEventListenerTest extends AbstractContainerBaseTest {

	@Autowired
	private PurchaseHistoryEventListener purchaseHistoryEventListener;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private PortfolioHoldingRepository portfolioHoldingRepository;

	@Autowired
	private PurchaseHistoryRepository purchaseHistoryRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private CurrentPriceRedisRepository currentPriceRedisRepository;

	@Autowired
	private FcmRepository fcmRepository;

	@Autowired
	private FirebaseMessaging mockedFirebaseMessaging;

	@DisplayName("매입 이력 이벤트 발생시 목표 수익률에 달성하여 푸시 알림을 한다")
	@Test
	void listenPurchaseHistory() throws FirebaseMessagingException {
		// given
		given(mockedFirebaseMessaging.send(any(Message.class)))
			.willReturn("projects/fineants-404407/messages/4754d355-5d5d-4f14-a642-75fecdb91fa5");

		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, LocalDateTime.now(), Count.from(100), Money.won(10000), "memo",
				portfolioHolding));
		fcmRepository.save(createFcmToken("token", member));
		currentPriceRedisRepository.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 50000L));

		PushNotificationEvent event = new PushNotificationEvent(
			PurchaseHistoryEventSendableParameter.create(portfolio.getId(), member.getId()));
		// when
		List<NotifyMessageItem> actual = purchaseHistoryEventListener.notifyTargetGainBy(event).join();
		// then
		assertThat(actual).hasSize(1);
		assertThat(notificationRepository.findAllByMemberId(member.getId())).hasSize(1);
	}

	@DisplayName("사용자는 매입 이벤트 발생 시 최대 손실율에 달성하여 푸시 알림을 받는다")
	@Test
	void addPurchaseHistory_whenAchieveMaxLoss_thenSaveNotification() throws FirebaseMessagingException {
		// given
		given(mockedFirebaseMessaging.send(any(Message.class)))
			.willReturn("projects/fineants-404407/messages/4754d355-5d5d-4f14-a642-75fecdb91fa5");

		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, LocalDateTime.now(), Count.from(1), Money.won(1000000), "memo",
				portfolioHolding));
		fcmRepository.save(createFcmToken("token", member));
		currentPriceRedisRepository.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 50000L));

		PushNotificationEvent event = new PushNotificationEvent(
			PurchaseHistoryEventSendableParameter.create(portfolio.getId(), member.getId()));
		// when
		List<NotifyMessageItem> actual = purchaseHistoryEventListener.notifyMaxLoss(event).join();
		// then
		assertThat(actual).hasSize(1);
		assertThat(notificationRepository.findAllByMemberId(member.getId())).hasSize(1);
	}
}
