package co.fineants.stock.presentation.dto.response;

import co.fineants.stock.domain.Market;
import co.fineants.stock.domain.Stock;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StockSearchItem {
	private String stockCode;
	private String tickerSymbol;
	private String companyName;
	private String companyNameEng;
	private Market market;

	public static StockSearchItem from(Stock stock) {
		return new StockSearchItem(stock.getStockCode(), stock.getTickerSymbol(), stock.getCompanyName(),
			stock.getCompanyNameEng(), stock.getMarket());
	}
}
