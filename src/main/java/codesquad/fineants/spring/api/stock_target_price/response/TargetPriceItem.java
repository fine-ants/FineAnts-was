package codesquad.fineants.spring.api.stock_target_price.response;

import java.time.LocalDateTime;

import codesquad.fineants.domain.target_price_notification.TargetPriceNotification;
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
public class TargetPriceItem {
	private Long notificationId;
	private Long targetPrice;
	private LocalDateTime dateAdded;

	public static TargetPriceItem from(TargetPriceNotification targetPriceNotification) {
		return TargetPriceItem.builder()
			.notificationId(targetPriceNotification.getId())
			.targetPrice(targetPriceNotification.getTargetPrice().getAmount().longValue())
			.dateAdded(targetPriceNotification.getCreateAt())
			.build();
	}
}
