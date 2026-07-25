package co.fineants.stock.event;

import org.springframework.context.ApplicationEvent;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class StockClosingPriceRefreshEvent extends ApplicationEvent {
	private final String tickerSymbol;

	public StockClosingPriceRefreshEvent(String tickerSymbol) {
		super(System.currentTimeMillis());
		this.tickerSymbol = tickerSymbol;
	}
}
