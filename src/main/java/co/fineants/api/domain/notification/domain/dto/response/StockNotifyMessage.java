package co.fineants.api.domain.notification.domain.dto.response;

import java.util.List;

import co.fineants.api.domain.common.money.Money;
import co.fineants.member.domain.Member;
import co.fineants.api.domain.notification.domain.entity.Notification;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class StockNotifyMessage extends NotifyMessage {

	private final String stockName;
	private final Money targetPrice;
	private final Long targetPriceNotificationId;
	
	@Override
	public NotifyMessage withMessageId(List<String> messageIds) {
		return this.toBuilder()
			.messageIds(messageIds)
			.build();
	}

	@Override
	public Notification toEntity(Member member) {
		return Notification.stockTargetPriceNotification(getTitle(), getReferenceId(), getLink(), member,
			getMessageIds(), getStockName(), getTargetPrice(),
			getTargetPriceNotificationId());
	}
}
