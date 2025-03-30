package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class TargetPriceNotificationDuplicateException extends DuplicateException {
	public TargetPriceNotificationDuplicateException(String tickerSymbol, Money targetPrice) {
		super(String.format("tickerSymbol=%s, targetPrice=%s", tickerSymbol, targetPrice),
			CustomErrorCode.TARGET_PRICE_NOTIFICATION_DUPLICATE);
	}
}
