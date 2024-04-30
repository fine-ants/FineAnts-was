package codesquad.fineants.domain.stock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.RateDivision;
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

	public Map<Integer, Money> createMonthlyDividends(List<PurchaseHistory> purchaseHistories,
		LocalDate currentLocalDate) {
		Map<Integer, Money> result = new HashMap<>();
		for (int month = 1; month <= 12; month++) {
			result.put(month, Money.zero());
		}

		List<StockDividend> currentYearStockDividends = stockDividends.stream()
			.filter(stockDividend -> stockDividend.isCurrentYearRecordDate(currentLocalDate))
			.collect(Collectors.toList());

		for (StockDividend stockDividend : currentYearStockDividends) {
			for (PurchaseHistory purchaseHistory : purchaseHistories) {
				if (stockDividend.isSatisfied(purchaseHistory.getPurchaseLocalDate())) {
					int paymentMonth = stockDividend.getMonthValueByPaymentDate();
					Money dividendSum = stockDividend.calculateDividendSum(purchaseHistory.getNumShares());
					result.put(paymentMonth, result.getOrDefault(paymentMonth, Money.zero()).add(dividendSum));
				}
			}
		}

		return result;
	}

	public Map<Integer, Money> createMonthlyExpectedDividends(List<PurchaseHistory> purchaseHistories,
		LocalDate currentLocalDate) {
		Map<Integer, Money> result = new HashMap<>();
		for (int month = 1; month <= 12; month++) {
			result.put(month, Money.zero());
		}

		// 0. 현재년도에 해당하는 배당금 정보를 필터링하여 별도 저장합니다.
		List<StockDividend> currentYearStockDividends = stockDividends.stream()
			.filter(stockDividend -> stockDividend.isCurrentYearRecordDate(currentLocalDate))
			.collect(Collectors.toList());

		// 1. 배당금 데이터 중에서 현금지급일자가 작년도에 해당하는 배당금 정보를 필터링합니다.
		// 2. 1단계에서 필터링한 배당금 데이터들중 0단계에서 별도 저장한 현재년도의 분기 배당금과 중복되는 배당금 정보를 필터링합니다.
		LocalDate lastYearLocalDate = currentLocalDate.minusYears(1L);
		stockDividends.stream()
			.filter(stockDividend -> stockDividend.isLastYearPaymentDate(lastYearLocalDate))
			.filter(stockDividend -> !stockDividend.isDuplicatedRecordDate(currentYearStockDividends))
			.forEach(stockDividend -> {
				// 3. 필터링한 배당금 정보들을 이용하여 배당금을 계산합니다.
				for (PurchaseHistory purchaseHistory : purchaseHistories) {
					int paymentMonth = stockDividend.getMonthValueByPaymentDate();
					Money dividendSum = stockDividend.calculateDividendSum(purchaseHistory.getNumShares());
					result.put(paymentMonth, result.getOrDefault(paymentMonth, Money.zero()).add(dividendSum));
				}
			});
		return result;
	}

	public Money getAnnualDividend() {
		return stockDividends.stream()
			.filter(dividend -> dividend.isCurrentYearPaymentDate(LocalDate.now()))
			.map(StockDividend::getDividend)
			.reduce(Money.zero(), Money::add);
	}

	public RateDivision getAnnualDividendYield(CurrentPriceManager manager) {
		Money dividends = stockDividends.stream()
			.filter(dividend -> dividend.isSatisfiedPaymentDateEqualYearBy(LocalDate.now()))
			.map(StockDividend::getDividend)
			.reduce(Money.zero(), Money::add);
		return dividends.divide(getCurrentPrice(manager));
	}

	public Money getDailyChange(CurrentPriceManager currentPriceManager,
		LastDayClosingPriceManager lastDayClosingPriceManager) {
		Money currentPrice = getCurrentPrice(currentPriceManager);
		Money closingPrice = getClosingPrice(lastDayClosingPriceManager);
		return currentPrice.subtract(closingPrice);
	}

	public RateDivision getDailyChangeRate(CurrentPriceManager currentPriceManager,
		LastDayClosingPriceManager lastDayClosingPriceManager) {
		Money currentPrice = currentPriceManager.getCurrentPrice(tickerSymbol).orElse(null);
		Money lastDayClosingPrice = lastDayClosingPriceManager.getClosingPrice(tickerSymbol).orElse(null);
		if (currentPrice == null || lastDayClosingPrice == null) {
			return null;
		}
		return currentPrice.minus(lastDayClosingPrice).divide(lastDayClosingPrice);
	}

	public Money getCurrentPrice(CurrentPriceManager manager) {
		return manager.getCurrentPrice(tickerSymbol).orElseGet(Money::zero);
	}

	public Money getClosingPrice(LastDayClosingPriceManager manager) {
		return manager.getClosingPrice(tickerSymbol).orElseGet(Money::zero);
	}

	public List<Integer> getDividendMonths() {
		return stockDividends.stream()
			.filter(dividend -> dividend.isCurrentYearPaymentDate(LocalDate.now()))
			.map(dividend -> dividend.getPaymentDate().getMonthValue())
			.collect(Collectors.toList());
	}

	// ticker 및 recordDate 기준으로 KisDividend가 매치되어 있는지 확인
	public boolean matchByTickerSymbolAndRecordDate(String tickerSymbol, LocalDate recordDate) {
		if (!this.tickerSymbol.equals(tickerSymbol)) {
			return false;
		}
		return stockDividends.stream()
			.anyMatch(s -> s.equalRecordDate(recordDate));
	}

	public Optional<StockDividend> getStockDividendBy(String tickerSymbol, LocalDate recordDate) {
		if (!this.tickerSymbol.equals(tickerSymbol)) {
			return Optional.empty();
		}
		return stockDividends.stream()
			.filter(s -> s.equalRecordDate(recordDate))
			.findAny();
	}

	public List<StockDividend> getStockDividendNotInRange(LocalDate from, LocalDate to) {
		return stockDividends.stream()
			.filter(stockDividend -> !stockDividend.hasInRange(from, to))
			.collect(Collectors.toList());
	}
}
