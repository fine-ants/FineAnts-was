package codesquad.fineants.spring.api.notification.response;

import codesquad.fineants.domain.notification.Notification;
import codesquad.fineants.domain.notification.StockTargetPriceNotification;
import codesquad.fineants.domain.notification.type.NotificationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@ToString
public class TargetPriceNotificationResponse {
	private Long notificationId;
	private Boolean isRead;
	private String title;
	private String content;
	private NotificationType type;
	private String referenceId;
	private Long memberId;
	private String link;
	private String stockName;
	private Long targetPrice;
	private Long targetPriceNotificationId;

	public static TargetPriceNotificationResponse from(Notification notification) {
		StockTargetPriceNotification priceNotification = (StockTargetPriceNotification)notification;
		return TargetPriceNotificationResponse.builder()
			.notificationId(priceNotification.getId())
			.isRead(priceNotification.getIsRead())
			.title(priceNotification.getTitle())
			.content(priceNotification.getContent())
			.type(priceNotification.getType())
			.referenceId(priceNotification.getReferenceId())
			.memberId(priceNotification.getMember().getId())
			.link(priceNotification.getLink())
			.stockName(priceNotification.getStockName())
			.targetPrice(priceNotification.getTargetPrice())
			.targetPriceNotificationId(priceNotification.getTargetPriceNotificationId())
			.build();
	}
}
