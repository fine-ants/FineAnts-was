package co.fineants.stock.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class StockCurrentPriceRefreshEvent extends ApplicationEvent {

	private final String tickerSymbol;

	public StockCurrentPriceRefreshEvent(String tickerSymbol) {
		super(System.currentTimeMillis());
		this.tickerSymbol = tickerSymbol;
	}

	@Override
	public String toString() {
		return "종목 현재가 갱신 이벤트 - tickerSymbol=" + tickerSymbol;
	}
}
