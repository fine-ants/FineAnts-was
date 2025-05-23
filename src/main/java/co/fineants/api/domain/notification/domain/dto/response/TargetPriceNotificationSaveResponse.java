package co.fineants.api.domain.notification.domain.dto.response;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.notification.domain.dto.response.save.NotificationSaveResponse;
import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.domain.entity.StockTargetPriceNotification;
import co.fineants.api.domain.notification.domain.entity.type.NotificationType;
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
public class TargetPriceNotificationSaveResponse implements NotificationSaveResponse {
	private Long notificationId;
	private Boolean isRead;
	private String title;
	private String content;
	private NotificationType type;
	private String referenceId;
	private Long memberId;
	private String link;
	private String stockName;
	private Money targetPrice;
	private Long targetPriceNotificationId;

	public static TargetPriceNotificationSaveResponse from(Notification notification) {
		StockTargetPriceNotification priceNotification = (StockTargetPriceNotification)notification;
		return TargetPriceNotificationSaveResponse.builder()
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
