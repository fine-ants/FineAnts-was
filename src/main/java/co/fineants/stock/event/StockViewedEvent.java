package co.fineants.stock.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class StockViewedEvent extends ApplicationEvent {
	private final String tickerSymbol;

	public StockViewedEvent(String tickerSymbol) {
		super(System.currentTimeMillis());
		this.tickerSymbol = tickerSymbol;
	}
}
