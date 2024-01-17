package codesquad.fineants.domain.stock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.stock.converter.MarketConverter;
import codesquad.fineants.domain.stock_dividend.StockDividend;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString(exclude = "stockDividends")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Stock extends BaseEntity {

	@Id
	private String tickerSymbol;
	private String companyName;
	private String companyNameEng;
	private String stockCode;
	private String sector;
	@Convert(converter = MarketConverter.class)
	private Market market;

	@OneToMany(mappedBy = "stock", fetch = FetchType.LAZY)
	private final List<StockDividend> stockDividends = new ArrayList<>();

	@Builder
	public Stock(String tickerSymbol, String companyName, String companyNameEng, String stockCode, String sector,
		Market market) {
		this.tickerSymbol = tickerSymbol;
		this.companyName = companyName;
		this.companyNameEng = companyNameEng;
		this.stockCode = stockCode;
		this.sector = sector;
		this.market = market;
	}

	public void addStockDividend(StockDividend stockDividend) {
		if (!stockDividends.contains(stockDividend)) {
			stockDividends.add(stockDividend);
		}
	}

	public List<StockDividend> getCurrentMonthDividends() {
		LocalDate today = LocalDate.now();
		return stockDividends.stream()
			.filter(dividend -> dividend.getPaymentDate() != null)
			.filter(dividend -> dividend.getPaymentDate().getYear() == today.getYear() &&
				dividend.getPaymentDate().getMonth() == today.getMonth())
			.collect(Collectors.toList());
	}

	public List<StockDividend> getCurrentYearDividends() {
		LocalDate today = LocalDate.now();
		return stockDividends.stream()
			.filter(dividend -> dividend.isCurrentYearPaymentDate(today))
			.collect(Collectors.toList());
	}

	public Map<Integer, Long> createMonthlyDividends(List<PurchaseHistory> purchaseHistories) {
		Map<Integer, Long> result = new HashMap<>();
		for (int month = 1; month <= 12; month++) {
			result.put(month, 0L);
		}

		for (StockDividend stockDividend : stockDividends) {
			for (PurchaseHistory purchaseHistory : purchaseHistories) {
				if (stockDividend.isSatisfied(purchaseHistory.getPurchaseLocalDate())) {
					int paymentMonth = stockDividend.getMonthValueByPaymentDate();
					long dividendSum = stockDividend.calculateDividendSum(purchaseHistory.getNumShares());
					result.put(paymentMonth, result.getOrDefault(paymentMonth, 0L) + dividendSum);
				}
			}
		}

		return result;
	}

	public Map<Integer, Long> createMonthlyExpectedDividends(List<PurchaseHistory> purchaseHistories) {
		Map<Integer, Long> result = new HashMap<>();
		for (int month = 1; month <= 12; month++) {
			result.put(month, 0L);
		}

		// 0. 현재년도에 해당하는 배당금 정보를 필터링하여 별도 저장합니다.
		LocalDate currentYearLocalDate = LocalDate.now();
		List<StockDividend> currentYearStockDividends = stockDividends.stream()
			.filter(stockDividend -> stockDividend.isCurrentYearRecordDate(currentYearLocalDate))
			.collect(Collectors.toList());

		// 1. 배당금 데이터 중에서 현금지급일자가 작년도에 해당하는 배당금 정보를 필터링합니다.
		// 2. 1단계에서 필터링한 배당금 데이터들중 0단계에서 별도 저장한 현재년도의 분기 배당금과 중복되는 배당금 정보를 필터링합니다.
		LocalDate lastYearLocalDate = LocalDate.now().minusYears(1L);
		stockDividends.stream()
			.filter(stockDividend -> stockDividend.isLastYearPaymentDate(lastYearLocalDate))
			.filter(stockDividend -> !stockDividend.isDuplicatedRecordDate(currentYearStockDividends))
			.forEach(stockDividend -> {
				// 3. 필터링한 배당금 정보들을 이용하여 배당금을 계산합니다.
				for (PurchaseHistory purchaseHistory : purchaseHistories) {
					int paymentMonth = stockDividend.getMonthValueByPaymentDate();
					long dividendSum = stockDividend.calculateDividendSum(purchaseHistory.getNumShares());
					result.put(paymentMonth, result.getOrDefault(paymentMonth, 0L) + dividendSum);
				}
			});
		return result;
	}

	public float getAnnualDividendYield(CurrentPriceManager manager) {
		long dividends = stockDividends.stream()
			.filter(dividend -> dividend.getPaymentDate().getYear() == LocalDate.now().getYear())
			.mapToLong(StockDividend::getDividend)
			.sum();
		long currentPrice = getCurrentPrice(manager);
		if (currentPrice == 0)
			return 0;
		return ((float)dividends / currentPrice) * 100;
	}

	public long getDailyChange(CurrentPriceManager currentPriceManager,
		LastDayClosingPriceManager lastDayClosingPriceManager) {
		return currentPriceManager.getCurrentPrice(tickerSymbol) - lastDayClosingPriceManager.getPrice(tickerSymbol);
	}

	public Long getCurrentPrice(CurrentPriceManager manager) {
		return manager.getCurrentPrice(tickerSymbol);
	}

	public Long getLastDayClosingPrice(LastDayClosingPriceManager manager) {
		return manager.getPrice(tickerSymbol);
	}
}
