package co.fineants.stock.domain.event;

import org.springframework.context.ApplicationEvent;

public class StockReloadEvent extends ApplicationEvent {

	public StockReloadEvent() {
		super(System.currentTimeMillis());
	}
}
