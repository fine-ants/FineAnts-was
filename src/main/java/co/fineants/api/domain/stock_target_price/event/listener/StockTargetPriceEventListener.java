package co.fineants.api.domain.stock_target_price.event.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import co.fineants.api.domain.notification.domain.dto.response.NotifyMessageResponse;
import co.fineants.api.domain.notification.service.NotificationService;
import co.fineants.api.domain.stock_target_price.event.domain.StockTargetPriceEventSendableParameter;
import co.fineants.api.domain.stock_target_price.event.domain.StockTargetPriceNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockTargetPriceEventListener {

	private final NotificationService service;

	@Async
	@EventListener
	public void notifyStockTargetPriceMessages(StockTargetPriceNotificationEvent event) {
		StockTargetPriceEventSendableParameter value = event.getValue();
		NotifyMessageResponse response = service.notifyTargetPriceToAllMember(value.getTickerSymbols());
		if (!response.isEmpty()) {
			log.info("종목 지정가 메시지 전송 결과 : response={}", response);
		}
	}
}
