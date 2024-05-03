package codesquad.fineants.spring.api.stock.response;

import java.util.List;

import codesquad.fineants.domain.common.money.Bank;
import codesquad.fineants.domain.common.money.Currency;
import codesquad.fineants.domain.common.money.Expression;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class StockResponse {
	private String stockCode;
	private String tickerSymbol;
	private String companyName;
	private String companyNameEng;
	private Market market;
	private Money currentPrice;
	private Money dailyChange;
	private Percentage dailyChangeRate;
	private String sector;
	private Money annualDividend;
	private Percentage annualDividendYield;
	private List<Integer> dividendMonths;

	public static StockResponse create(String stockCode, String tickerSymbol, String companyName,
		String companyNameEng, Market market, Expression currentPrice, Expression dailyChange,
		Percentage dailyChangeRate, String sector, Expression annualDividend, Percentage annualDividendYield,
		List<Integer> dividendMonths) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		return StockResponse.builder()
			.stockCode(stockCode)
			.tickerSymbol(tickerSymbol)
			.companyName(companyName)
			.companyNameEng(companyNameEng)
			.market(market)
			.currentPrice(currentPrice.reduce(bank, to))
			.dailyChange(dailyChange.reduce(bank, to))
			.dailyChangeRate(dailyChangeRate)
			.sector(sector)
			.annualDividend(annualDividend.reduce(bank, to))
			.annualDividendYield(annualDividendYield)
			.dividendMonths(dividendMonths)
			.build();
	}

	public static StockResponse of(Stock stock, CurrentPriceManager currentPriceManager,
		LastDayClosingPriceManager lastDayClosingPriceManager) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		return StockResponse.builder()
			.stockCode(stock.getStockCode())
			.tickerSymbol(stock.getTickerSymbol())
			.companyName(stock.getCompanyName())
			.companyNameEng(stock.getCompanyNameEng())
			.market(stock.getMarket())
			.currentPrice(stock.getCurrentPrice(currentPriceManager))
			.dailyChange(stock.getDailyChange(currentPriceManager, lastDayClosingPriceManager).reduce(bank, to))
			.dailyChangeRate(stock.getDailyChangeRate(currentPriceManager, lastDayClosingPriceManager).toPercentage(
				bank, to))
			.sector(stock.getSector())
			.annualDividend(stock.getAnnualDividend().reduce(bank, to))
			.annualDividendYield(
				stock.getAnnualDividendYield(currentPriceManager).toPercentage(bank, to))
			.dividendMonths(stock.getDividendMonths())
			.build();
	}
}
