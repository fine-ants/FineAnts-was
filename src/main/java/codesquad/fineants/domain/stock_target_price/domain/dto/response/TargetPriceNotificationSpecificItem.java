package codesquad.fineants.domain.stock_target_price.domain.dto.response;

import java.time.LocalDateTime;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.stock_target_price.domain.entity.TargetPriceNotification;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class TargetPriceNotificationSpecificItem {
	private Long notificationId;
	private Money targetPrice;
	private LocalDateTime dateAdded;

	public static TargetPriceNotificationSpecificItem from(TargetPriceNotification targetPriceNotification) {
		return TargetPriceNotificationSpecificItem.builder()
			.notificationId(targetPriceNotification.getId())
			.targetPrice(targetPriceNotification.getTargetPrice())
			.dateAdded(targetPriceNotification.getCreateAt())
			.build();
	}
}
