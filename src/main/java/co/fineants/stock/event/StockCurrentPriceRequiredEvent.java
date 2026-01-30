package co.fineants.stock.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class StockCurrentPriceRequiredEvent extends ApplicationEvent {
	private final String tickerSymbol;

	public StockCurrentPriceRequiredEvent(String tickerSymbol) {
		super(System.currentTimeMillis());
		this.tickerSymbol = tickerSymbol;
	}
}
