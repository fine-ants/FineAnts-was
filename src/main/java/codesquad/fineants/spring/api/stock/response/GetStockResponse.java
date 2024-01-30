package codesquad.fineants.spring.api.stock.response;

import java.util.List;

import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetStockResponse {
	private String stockCode;
	private String tickerSymbol;
	private String companyName;
	private String companyNameEng;
	private Market market;
	private Long currentPrice;
	private Long dailyChange;
	private Float dailyChangeRate;
	private String sector;
	private Long annualDividend;
	private Float annualDividendYield;
	private List<Integer> dividendMonths;

	public static GetStockResponse of(Stock stock, CurrentPriceManager currentPriceManager,
		LastDayClosingPriceManager lastDayClosingPriceManager) {
		return GetStockResponse.builder()
			.stockCode(stock.getStockCode())
			.tickerSymbol(stock.getTickerSymbol())
			.companyName(stock.getCompanyName())
			.companyNameEng(stock.getCompanyNameEng())
			.market(stock.getMarket())
			.currentPrice(stock.getCurrentPrice(currentPriceManager))
			.dailyChange(stock.getDailyChange(currentPriceManager, lastDayClosingPriceManager))
			.dailyChangeRate(stock.getDailyChangeRate(currentPriceManager, lastDayClosingPriceManager))
			.sector(stock.getSector())
			.annualDividend(stock.getAnnualDividend())
			.annualDividendYield(stock.getAnnualDividendYield(currentPriceManager))
			.dividendMonths(stock.getDividendMonths())
			.build();
	}
}