package co.fineants.api.domain.notification.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.common.notification.PortfolioMaximumLossNotifiable;
import co.fineants.api.domain.common.notification.PortfolioTargetGainNotifiable;
import co.fineants.api.domain.common.notification.TargetPriceNotificationNotifiable;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessage;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessageItem;
import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
import co.fineants.api.domain.stock_target_price.domain.entity.TargetPriceNotification;
import co.fineants.api.domain.stock_target_price.repository.StockTargetPriceRepository;
import co.fineants.api.global.errors.errorcode.PortfolioErrorCode;
import co.fineants.api.global.errors.exception.FineAntsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class NotificationService {
	private final PortfolioRepository portfolioRepository;
	private final NotificationRepository notificationRepository;
	private final MemberRepository memberRepository;
	private final StockTargetPriceRepository stockTargetPriceRepository;
	private final PortfolioCalculator portfolioCalculator;
	private final CurrentPriceRedisRepository currentPriceRedisRepository;
	private final NotifyMessageFactory notifyMessageFactory;
	private final NotificationSender notificationSender;
	private final TargetGainNotificationStrategy targetGainNotificationStrategy;
	private final MaximumLossNotificationStrategy maximumLossNotificationStrategy;
	private final TargetPriceNotificationStrategy targetPriceNotificationStrategy;

	/**
	 * 특정 포트폴리오의 목표 수익률 달성 알림 푸시
	 *
	 * @param portfolioId 포트폴리오 등록번호
	 * @return 알림 전송 결과
	 */
	@Transactional
	public List<NotifyMessageItem> notifyTargetGain(Long portfolioId) {
		Notifiable notifiable = portfolioRepository.findByPortfolioIdWithAll(portfolioId).stream()
			.findAny()
			.map(p -> {
				boolean isReachedTargetGain = portfolioCalculator.reachedTargetGainBy(p);
				return PortfolioTargetGainNotifiable.from(p, isReachedTargetGain);
			})
			.map(Notifiable.class::cast)
			.orElseThrow(() -> new FineAntsException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
		return notifyTargetGainAll(List.of(notifiable));
	}

	@Transactional
	public List<NotifyMessageItem> notifyTargetGainAll() {
		List<Notifiable> notifiableList = portfolioRepository.findAllWithAll().stream()
			.map(portfolio -> {
				boolean isReachedTargetGain = portfolioCalculator.reachedTargetGainBy(portfolio);
				return PortfolioTargetGainNotifiable.from(portfolio, isReachedTargetGain);
			})
			.map(Notifiable.class::cast)
			.toList();
		return notifyTargetGainAll(notifiableList);
	}

	private List<NotifyMessageItem> notifyTargetGainAll(List<Notifiable> notifiableList) {
		return notifyMessages(
			notifiableList,
			targetGainNotificationStrategy
		);
	}

	private List<NotifyMessageItem> notifyMessages(
		List<Notifiable> data,
		NotificationStrategy strategy) {
		// 알림 조건을 만족하는 데이터를 생성
		List<NotifyMessage> notifyMessages = notifyMessageFactory.generate(data, strategy.getPolicy());

		// 만족하는 포트폴리오를 대상으로 알림 데이터 생성 & 알림 전송
		List<NotifyMessage> sentNotifyMessages = notificationSender.send(notifyMessages);

		// 알림 전송에 실패한 전송 메시지에 대해서 FCM 토큰 삭제
		notificationSender.deleteTokensForFailedMessagesIn(sentNotifyMessages);

		// 알림 저장
		List<Notification> notifications = saveNotifications(sentNotifyMessages);

		// 전송 내역 저장
		notifications.forEach(strategy.getSendHistory());

		// 결과 객체 생성
		return notifications.stream()
			.map(strategy.getMapper())
			.sorted()
			.toList();
	}

	@NotNull
	private List<Notification> saveNotifications(List<NotifyMessage> notifyMessages) {
		List<Notification> notifications = notifyMessages.stream()
			.map(notifyMessage -> {
				Member member = memberRepository.findById(notifyMessage.getMemberId())
					.orElseThrow(() -> notFoundMember(notifyMessage));
				return notifyMessage.toEntity(member);
			})
			.toList();
		return notificationRepository.saveAll(notifications);
	}

	@NotNull
	private static IllegalArgumentException notFoundMember(NotifyMessage notifyMessage) {
		return new IllegalArgumentException("not found member, memberId=" + notifyMessage.getMemberId());
	}

	/**
	 * 특정 포트폴리오의 최대 손실율 달성 알림 푸시
	 *
	 * @param portfolioId 포트폴리오 등록번호
	 * @return 알림 전송 결과
	 */
	@Transactional
	public List<NotifyMessageItem> notifyMaxLoss(Long portfolioId) {
		Notifiable notifiable = portfolioRepository.findByPortfolioIdWithAll(portfolioId).stream()
			.findAny()
			.map(portfolio -> {
				boolean isReached = portfolioCalculator.reachedMaximumLossBy(portfolio);
				return PortfolioMaximumLossNotifiable.from(portfolio, isReached);
			})
			.orElseThrow(() -> new FineAntsException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
		return notifyMaxLossAll(List.of(notifiable));
	}

	/**
	 * 모든 포트폴리오를 대상으로 최대 손실율에 도달하는 모든 포트폴리오에 대해서 최대 손실율 도달 알림 푸시
	 *
	 * @return 알림 전송 결과
	 */
	@Transactional
	public List<NotifyMessageItem> notifyMaxLossAll() {
		List<Notifiable> notifiableList = portfolioRepository.findAllWithAll().stream()
			.map(portfolio -> {
				boolean isReached = portfolioCalculator.reachedMaximumLossBy(portfolio);
				return PortfolioMaximumLossNotifiable.from(portfolio, isReached);
			})
			.map(Notifiable.class::cast)
			.toList();
		return notifyMaxLossAll(notifiableList);
	}

	private List<NotifyMessageItem> notifyMaxLossAll(List<Notifiable> notifiableList) {
		return notifyMessages(
			notifiableList,
			maximumLossNotificationStrategy
		);
	}

	/**
	 * 특정 회원을 대상으로 종목 지정가 알림 발송
	 *
	 * @param memberId 회원의 등록번호
	 * @return 알림 전송 결과
	 */
	@Transactional
	public List<NotifyMessageItem> notifyTargetPrice(Long memberId) {
		List<Notifiable> notifiableList = stockTargetPriceRepository.findAllByMemberId(memberId)
			.stream()
			.map(StockTargetPrice::getTargetPriceNotifications)
			.flatMap(Collection::stream)
			.sorted(Comparator.comparingLong(TargetPriceNotification::getId))
			.map(targetPriceNotification -> {
				boolean isReached = targetPriceNotification.isSameTargetPrice(currentPriceRedisRepository);
				return TargetPriceNotificationNotifiable.from(targetPriceNotification, isReached);
			})
			.map(Notifiable.class::cast)
			.toList();
		return notifyTargetPriceAll(notifiableList);
	}

	/**
	 * 모든 회원을 대상으로 특정 종목들에 대한 종목 지정가 알림 발송
	 *
	 * @param tickerSymbols 종목의 티커 심볼 리스트
	 * @return 알림 전송 결과
	 */
	@Transactional
	public List<NotifyMessageItem> notifyTargetPriceBy(List<String> tickerSymbols) {
		List<Notifiable> notifiableList = stockTargetPriceRepository.findAllByTickerSymbols(
				tickerSymbols)
			.stream()
			.map(StockTargetPrice::getTargetPriceNotifications)
			.flatMap(Collection::stream)
			.map(targetPriceNotification -> {
				boolean isReached = targetPriceNotification.isSameTargetPrice(currentPriceRedisRepository);
				return TargetPriceNotificationNotifiable.from(targetPriceNotification, isReached);
			})
			.map(Notifiable.class::cast)
			.toList();
		log.debug("notifiableList : {}", notifiableList);
		return notifyTargetPriceAll(notifiableList);
	}

	private List<NotifyMessageItem> notifyTargetPriceAll(List<Notifiable> notifiableList) {
		return notifyMessages(
			notifiableList,
			targetPriceNotificationStrategy
		);
	}
}
