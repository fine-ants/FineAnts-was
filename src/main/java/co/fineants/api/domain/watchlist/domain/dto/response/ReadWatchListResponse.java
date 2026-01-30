package co.fineants.api.domain.watchlist.domain.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.kis.service.ClosingPriceService;
import co.fineants.api.domain.kis.service.CurrentPriceService;
import co.fineants.api.domain.watchlist.domain.entity.WatchStock;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockDividend;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReadWatchListResponse {
	private String name;
	private List<WatchStockResponse> watchStocks;

	@Builder
	@Getter
	public static class WatchStockResponse {
		private Long id;
		private String companyName;
		private String tickerSymbol;
		private Money currentPrice;
		private Money dailyChange;
		private Percentage dailyChangeRate;
		private Percentage annualDividendYield;
		private String sector;
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
		private LocalDateTime dateAdded;
	}

	// TODO: CurrentPriceService, ClosingPriceService 주입 방식 개선, 필요한 필드값을 서비스 레이어에서 빌더와 같은 방식으로 넘기기
	public static ReadWatchListResponse.WatchStockResponse from(WatchStock watchStock,
		LocalDateTimeService localDateTimeService,
		CurrentPriceService currentPriceService,
		ClosingPriceService closingPriceService) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		Stock stock = watchStock.getStock();

		Money currentPrice = currentPriceService.fetchPrice(stock.getTickerSymbol());
		Money lastDayClosingPrice = closingPriceService.fetchPrice(stock.getTickerSymbol());
		Money dailyChange = currentPrice.minus(lastDayClosingPrice).reduce(bank, to);
		Percentage dailyChangeRate = currentPrice.minus(lastDayClosingPrice).divide(lastDayClosingPrice)
			.toPercentage(bank, to);
		return ReadWatchListResponse.WatchStockResponse.builder()
			.id(watchStock.getId())
			.companyName(stock.getCompanyName())
			.tickerSymbol(stock.getTickerSymbol())
			.currentPrice(currentPrice.reduce(bank, to))
			.dailyChange(dailyChange)
			.dailyChangeRate(dailyChangeRate)
			.annualDividendYield(getAnnualDividendYield(stock, currentPrice, localDateTimeService))
			.sector(stock.getSector())
			.dateAdded(watchStock.getCreateAt())
			.build();
	}

	private static Percentage getAnnualDividendYield(Stock stock, Money currentPrice,
		LocalDateTimeService localDateTimeService) {
		Expression dividends = stock.getStockDividends().stream()
			.filter(dividend -> dividend.isPaymentInCurrentYear(localDateTimeService.getLocalDateWithNow()))
			.map(StockDividend::getDividend)
			.map(Expression.class::cast)
			.reduce(Money.zero(), Expression::plus);
		return dividends.divide(currentPrice).toPercentage(Bank.getInstance(), Currency.KRW);
	}
}
