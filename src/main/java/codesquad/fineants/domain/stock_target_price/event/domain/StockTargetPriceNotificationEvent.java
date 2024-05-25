package codesquad.fineants.domain.stock_target_price.event.domain;

import codesquad.fineants.domain.purchasehistory.event.domain.EventHoldingValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StockTargetPriceNotificationEvent implements EventHoldingValue<StockTargetPriceEventSendableParameter> {
	private StockTargetPriceEventSendableParameter value;

	public StockTargetPriceNotificationEvent(StockTargetPriceEventSendableParameter value) {
		this.value = value;
	}
}
