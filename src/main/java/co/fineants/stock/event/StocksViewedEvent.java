package co.fineants.stock.event;

import java.util.Collection;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class StocksViewedEvent extends ApplicationEvent {
	private final Collection<String> tickerSymbols;

	public StocksViewedEvent(Collection<String> tickerSymbols) {
		super(System.currentTimeMillis());
		this.tickerSymbols = tickerSymbols;
	}
}
