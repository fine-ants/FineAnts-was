package co.fineants.stock.domain;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import co.fineants.api.domain.BaseEntity;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.RateDivision;
import co.fineants.api.domain.kis.domain.ClosingPriceRedisEntity;
import co.fineants.api.domain.kis.domain.CurrentPriceRedisEntity;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.PriceRepository;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.global.common.csv.CsvLineConvertible;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.stock.infrastructure.MarketConverter;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString(exclude = "stockDividends")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "stockCode", callSuper = false)
@Entity
public class Stock extends BaseEntity implements CsvLineConvertible {

	@Id
	private String tickerSymbol;
	private String companyName;
	private String companyNameEng;
	private String stockCode;
	private String sector;
	@Convert(converter = MarketConverter.class)
	private Market market;
	private boolean isDeleted;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(
		name = "stock_dividend",
		joinColumns = @JoinColumn(name = "ticker_symbol", nullable = false),
		uniqueConstraints = {
			@jakarta.persistence.UniqueConstraint(
				name = "uk_stock_dividend_ticker_symbol_record_date",
				columnNames = {"ticker_symbol", "record_date"}
			)
		}
	)
	@OrderColumn(name = "line_idx", nullable = false)
	private final List<StockDividend> stockDividends = new ArrayList<>();

	private static final String TICKER_PREFIX = "TS";

	private Stock(String tickerSymbol, String companyName, String companyNameEng, String stockCode, String sector,
		Market market) {
		this.tickerSymbol = tickerSymbol;
		this.companyName = companyName;
		this.companyNameEng = companyNameEng;
		this.stockCode = stockCode;
		this.sector = sector;
		this.market = market;
		this.isDeleted = false;
	}

	public static Stock of(String tickerSymbol, String companyName, String companyNameEng, String stockCode,
		String sector, Market market) {
		return new Stock(tickerSymbol, companyName, companyNameEng, stockCode, sector, market);
	}

	public static Stock delisted(String tickerSymbol, String companyName, String companyNameEng, String stockCode,
		String sector, Market market) {
		Stock stock = new Stock(tickerSymbol, companyName, companyNameEng, stockCode, sector, market);
		stock.isDeleted = true;
		return stock;
	}

	public void addStockDividend(StockDividend stockDividend) {
		if (!stockDividends.contains(stockDividend)) {
			stockDividends.add(stockDividend);
		}
	}

	public void removeStockDividend(StockDividend stockDividend) {
		stockDividends.remove(stockDividend);
	}

	public List<StockDividend> getCurrentMonthDividends(LocalDateTimeService localDateTimeService) {
		LocalDate today = localDateTimeService.getLocalDateWithNow();
		return stockDividends.stream()
			.filter(dividend -> dividend.isCurrentMonthPaymentDate(today))
			.toList();
	}

	public List<StockDividend> getCurrentYearDividends(LocalDateTimeService localDateTimeService) {
		LocalDate today = localDateTimeService.getLocalDateWithNow();
		return stockDividends.stream()
			.filter(dividend -> dividend.isCurrentYearPaymentDate(today))
			.toList();
	}

	public Map<Month, Expression> createMonthlyDividends(List<PurchaseHistory> purchaseHistories,
		LocalDate currentLocalDate) {
		Map<Month, Expression> result = initMonthlyDividendMap();
		List<StockDividend> currentYearStockDividends = getStockDividendsWithCurrentYearRecordDateBy(
			currentLocalDate);

		for (StockDividend stockDividend : currentYearStockDividends) {
			for (PurchaseHistory purchaseHistory : purchaseHistories) {
				if (stockDividend.canReceiveDividendOn(purchaseHistory)) {
					Month paymentMonth = stockDividend.getMonthByPaymentDate();
					Expression dividendSum = stockDividend.calculateDividendSum(purchaseHistory.getNumShares());
					Expression sum = result.getOrDefault(paymentMonth, Money.zero()).plus(dividendSum);
					result.put(paymentMonth, sum);
				}
			}
		}
		return result;
	}

	@NotNull
	private List<StockDividend> getStockDividendsWithCurrentYearRecordDateBy(LocalDate currentLocalDate) {
		return stockDividends.stream()
			.filter(stockDividend -> stockDividend.isCurrentYearRecordDate(currentLocalDate))
			.toList();
	}

	public Map<Month, Expression> createMonthlyExpectedDividends(List<PurchaseHistory> purchaseHistories,
		LocalDate currentLocalDate) {
		Map<Month, Expression> result = initMonthlyDividendMap();
		// 0. 현재년도에 해당하는 배당금 정보를 필터링하여 별도 저장합니다.
		List<StockDividend> currentYearStockDividends = getStockDividendsWithCurrentYearRecordDateBy(
			currentLocalDate);

		// 1. 배당금 데이터 중에서 현금지급일자가 작년도에 해당하는 배당금 정보를 필터링합니다.
		// 2. 1단계에서 필터링한 배당금 데이터들중 0단계에서 별도 저장한 현재년도의 분기 배당금과 중복되는 배당금 정보를 필터링합니다.
		LocalDate lastYearLocalDate = currentLocalDate.minusYears(1L);
		stockDividends.stream()
			.filter(stockDividend -> stockDividend.isLastYearPaymentDate(lastYearLocalDate))
			.filter(stockDividend -> !stockDividend.isDuplicatedRecordDate(currentYearStockDividends))
			.forEach(stockDividend -> {
				// 3. 필터링한 배당금 정보들을 이용하여 배당금을 계산합니다.
				for (PurchaseHistory purchaseHistory : purchaseHistories) {
					Month paymentMonth = stockDividend.getMonthByPaymentDate();
					Expression dividendSum = stockDividend.calculateDividendSum(purchaseHistory.getNumShares());
					result.put(paymentMonth, result.getOrDefault(paymentMonth, Money.zero()).plus(dividendSum));
				}
			});
		return result;
	}

	@NotNull
	private static Map<Month, Expression> initMonthlyDividendMap() {
		Map<Month, Expression> result = new EnumMap<>(Month.class);
		for (Month month : Month.values()) {
			result.put(month, Money.zero());
		}
		return result;
	}

	/**
	 * 올해 연간 배당금 합계를 반환한다.
	 * @param localDateTimeService 현지 시간 서비스
	 * @return 올해 연간 배당금 합계
	 */
	public Expression getAnnualDividend(LocalDateTimeService localDateTimeService) {
		return stockDividends.stream()
			.filter(dividend -> dividend.isCurrentYearPaymentDate(localDateTimeService.getLocalDateWithNow()))
			.map(StockDividend::getDividend)
			.map(Expression.class::cast)
			.reduce(Money.zero(), Expression::plus);
	}

	public RateDivision getAnnualDividendYield(PriceRepository manager,
		LocalDateTimeService localDateTimeService) {
		Expression dividends = stockDividends.stream()
			.filter(dividend -> dividend.isPaymentInCurrentYear(localDateTimeService.getLocalDateWithNow()))
			.map(StockDividend::getDividend)
			.map(Expression.class::cast)
			.reduce(Money.zero(), Expression::plus);
		return dividends.divide(getCurrentPrice(manager));
	}

	public Expression getCurrentPrice(PriceRepository priceRepository) {
		return priceRepository.fetchPriceBy(tickerSymbol)
			.map(CurrentPriceRedisEntity::getPriceMoney)
			.orElseGet(Money::zero);
	}

	public Expression getClosingPrice(ClosingPriceRepository repository) {
		return repository.fetchPrice(tickerSymbol)
			.map(ClosingPriceRedisEntity::getPriceMoney)
			.orElseGet(Money::zero);
	}

	public List<Month> getDividendMonths(LocalDateTimeService localDateTimeService) {
		return stockDividends.stream()
			.filter(dividend -> dividend.isCurrentYearPaymentDate(localDateTimeService.getLocalDateWithNow()))
			.map(StockDividend::getMonthByPaymentDate)
			.toList();
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
			.filter(stockDividend -> !stockDividend.hasInRangeForRecordDate(from, to))
			.toList();
	}

	@Override
	public String toCsvLine() {
		String ticker = String.format("%s%s", TICKER_PREFIX, tickerSymbol);
		return String.join("$",
			stockCode,
			ticker,
			companyName,
			companyNameEng,
			sector,
			market.name());
	}

	public List<StockDividend> getStockDividends() {
		return Collections.unmodifiableList(stockDividends);
	}

	public void clearStockDividend() {
		stockDividends.clear();
	}
}
