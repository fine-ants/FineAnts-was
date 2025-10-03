package co.fineants.api.domain.stock.domain.entity;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.MoneyConverter;
import co.fineants.api.domain.dividend.domain.entity.DividendDates;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Embeddable
@EqualsAndHashCode(of = {"dividendDates"})
public class StockDividendTemp {

	@Getter
	@Convert(converter = MoneyConverter.class)
	@Column(precision = 19, nullable = false)
	private Money dividend;

	@Embedded
	private DividendDates dividendDates;

	@Column(name = "is_deleted", nullable = false)
	private boolean isDeleted;

	@Transient
	private String tickerSymbol;

	protected StockDividendTemp() {
	}

	public StockDividendTemp(Money dividend, DividendDates dividendDates, boolean isDeleted, String tickerSymbol) {
		this.dividend = dividend;
		this.dividendDates = dividendDates;
		this.isDeleted = isDeleted;
		this.tickerSymbol = tickerSymbol;
	}

	public Expression calculateDividendSum(Count numShares) {
		return numShares.multiply(dividend);
	}

	public boolean isCurrentMonthPaymentDate(LocalDate today) {
		return dividendDates.isCurrentMonthPaymentDate(today);
	}

	public boolean isPurchaseDateBeforeExDividendDate(PurchaseHistory history) {
		return dividendDates.isPurchaseDateBeforeExDividendDate(history);
	}

	public boolean isCurrentYearPaymentDate(LocalDate today) {
		return dividendDates.isCurrentYearPaymentDate(today);
	}

	public boolean isSatisfiedBy(PurchaseHistory history) {
		return dividendDates.isSatisfiedBy(history);
	}

	public boolean isCurrentYearRecordDate(LocalDate localDate) {
		return dividendDates.isCurrentYearRecordDate(localDate);
	}

	public boolean canReceiveDividendOn(PurchaseHistory history) {
		return history.canReceiveDividendOn(dividendDates);
	}

	public Month getMonthByPaymentDate() {
		return dividendDates.getPaymentDateMonth();
	}

	public boolean isLastYearPaymentDate(LocalDate lastYearLocalDate) {
		return dividendDates.isLastYearPaymentDate(lastYearLocalDate);
	}

	public boolean isDuplicatedRecordDate(List<StockDividendTemp> currentYearStockDividends) {
		return currentYearStockDividends.stream()
			.anyMatch(stockDividend -> stockDividend.getQuarter().equals(getQuarter()));
	}

	private Integer getQuarter() {
		return dividendDates.getQuarterWithRecordDate();
	}

	public boolean isPaymentInCurrentYear(LocalDate localDate) {
		return dividendDates.isPaymentInCurrentYear(localDate);
	}

	public boolean equalRecordDate(LocalDate recordDate) {
		return dividendDates.equalRecordDate(recordDate);
	}

	public boolean hasInRangeForRecordDate(LocalDate from, LocalDate to) {
		return dividendDates.hasInRangeForRecordDate(from, to);
	}

	public boolean hasPaymentDate() {
		return dividendDates.hasPaymentDate();
	}

	// TODO: add test
	public void change(StockDividendTemp changeStockDividend) {
		this.dividend = changeStockDividend.dividend;
		this.dividendDates = changeStockDividend.dividendDates;
		this.isDeleted = changeStockDividend.isDeleted;
	}

	/**
	 * 배당 일정 정보들을 파싱하여 반환
	 * format :  tickerSymbol:dividend:recordDate:exDividendDate:paymentDate
	 *   - ex) 005930:361:2022-08-01:2022-08-01:2022-08-01, 005930:361:2022-08-01:2022-08-01:null
	 * @return 배당 일정 정보 요약
	 */
	public String parse(String tickerSymbol) {
		String dividendDateString = dividendDates.parse();
		return String.format("%s:%s:%s", tickerSymbol, dividend, dividendDateString);
	}

	@Override
	public String toString() {
		return String.format("배당금(배당금=%s, 배당일정=%s, 삭제여부=%s)", dividend, dividendDates, isDeleted);
	}
}
