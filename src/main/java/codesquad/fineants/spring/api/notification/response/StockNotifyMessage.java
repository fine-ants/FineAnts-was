package codesquad.fineants.spring.api.notification.response;

import codesquad.fineants.domain.notification.type.NotificationType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class StockNotifyMessage extends NotifyMessage {

	private final String stockName;
	private final Long targetPrice;
	private final Long targetPriceNotificationId;

	@Builder(access = AccessLevel.PRIVATE)
	public StockNotifyMessage(String title, String content, NotificationType type, String referenceId, Long memberId,
		String token, String link, String stockName, Long targetPrice, Long targetPriceNotificationId) {
		super(title, content, type, referenceId, memberId, token, link);
		this.stockName = stockName;
		this.targetPrice = targetPrice;
		this.targetPriceNotificationId = targetPriceNotificationId;
	}

	public static NotifyMessage create(String title, String content, NotificationType type, String referenceId,
		Long memberId, String token, String link, String stockName, Long targetPrice, Long targetPriceNotificationId) {
		return StockNotifyMessage.builder()
			.title(title)
			.content(content)
			.type(type)
			.referenceId(referenceId)
			.memberId(memberId)
			.token(token)
			.link(link)
			.stockName(stockName)
			.targetPrice(targetPrice)
			.targetPriceNotificationId(targetPriceNotificationId)
			.build();
	}
}
