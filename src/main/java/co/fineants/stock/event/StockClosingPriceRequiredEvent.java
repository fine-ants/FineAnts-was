package co.fineants.stock.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class StockClosingPriceRequiredEvent extends ApplicationEvent {
	private final String tickerSymbol;

	public StockClosingPriceRequiredEvent(String tickerSymbol) {
		super(System.currentTimeMillis());
		this.tickerSymbol = tickerSymbol;
	}
}
