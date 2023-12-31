package codesquad.fineants.spring.api.watch_list.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import codesquad.fineants.domain.watch_stock.WatchStock;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReadWatchListResponse {
	private long id;
	private String companyName;
	private String tickerSymbol;
	private long currentPrice;
	private long dailyChange;
	private float dailyChangeRate;
	private float annualDividendYield;
	private String sector;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime dateAdded;

	public static ReadWatchListResponse from(WatchStock watchStock, CurrentPriceManager currentPriceManager,
		LastDayClosingPriceManager lastDayClosingPriceManager) {
		return ReadWatchListResponse.builder()
			.id(watchStock.getId())
			.companyName(watchStock.getStock().getCompanyName())
			.tickerSymbol(watchStock.getStock().getTickerSymbol())
			.currentPrice(watchStock.getStock().getCurrentPrice(currentPriceManager))
			.dailyChange(watchStock.getStock().getDailyChange(currentPriceManager, lastDayClosingPriceManager))
			.annualDividendYield(watchStock.getStock().getAnnualDividendYield(currentPriceManager))
			.sector(watchStock.getStock().getSector())
			.dateAdded(watchStock.getCreateAt())
			.build();
	}
}
