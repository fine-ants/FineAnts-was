package co.fineants.api.domain.dividend.domain.entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.util.Strings;

import co.fineants.api.domain.dividend.domain.calculator.ExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.reader.HolidayFileReader;
import co.fineants.api.domain.kis.repository.HolidayRepository;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "recordDate")
public class DividendDates {
	@Column(name = "record_date", nullable = false)
	private LocalDate recordDate;
	@Column(name = "ex_dividend_date", nullable = false)
	private LocalDate exDividendDate;
	@Column(name = "payment_date")
	private LocalDate paymentDate;

	private static final ExDividendDateCalculator EX_DIVIDEND_DATE_CALCULATOR = new ExDividendDateCalculator(
		new HolidayRepository(new HolidayFileReader()));

	public static DividendDates withRecordDateOnly(LocalDate recordDate) {
		LocalDate exDividendDate = EX_DIVIDEND_DATE_CALCULATOR.calculate(recordDate);
		return new DividendDates(recordDate, exDividendDate, null);
	}

	public static DividendDates withPaymentDate(LocalDate recordDate, LocalDate paymentDate) {
		LocalDate exDividendDate = EX_DIVIDEND_DATE_CALCULATOR.calculate(recordDate);
		return new DividendDates(recordDate, exDividendDate, paymentDate);
	}

	public static DividendDates of(LocalDate recordDate, LocalDate exDividendDate, LocalDate paymentDate) {
		return new DividendDates(recordDate, exDividendDate, paymentDate);
	}

	public boolean canReceiveDividendOn(LocalDate purchaseDate) {
		if (paymentDate == null) {
			return false;
		}
		return purchaseDate.isBefore(exDividendDate);
	}

	public Integer getPaymentDateMonth() {
		return paymentDate.getMonthValue();
	}

	public boolean isCurrentYearRecordDate(LocalDate localDate) {
		return localDate.getYear() == recordDate.getYear();
	}

	public boolean isLastYearPaymentDate(LocalDate lastYearLocalDate) {
		if (paymentDate == null) {
			return false;
		}
		return lastYearLocalDate.getYear() == paymentDate.getYear();
	}

	public Integer getQuarterWithRecordDate() {
		return recordDate.getMonthValue() / 4 + 1;
	}

	public boolean isCurrentYearPaymentDate(LocalDate today) {
		if (paymentDate == null) {
			return false;
		}
		return today.getYear() == paymentDate.getYear();
	}

	public boolean equalRecordDate(LocalDate recordDate) {
		return this.recordDate.equals(recordDate);
	}

	public boolean hasPaymentDate() {
		return paymentDate != null;
	}

	public String parse() {
		return String.format("%s:%s:%s", recordDate, exDividendDate, paymentDate);
	}

	public boolean isPaymentInCurrentYear(LocalDate localDate) {
		if (paymentDate == null) {
			return false;
		}
		return localDate.getYear() == paymentDate.getYear();
	}

	public boolean hasInRangeForRecordDate(LocalDate from, LocalDate to) {
		return recordDate.isAfter(from) && recordDate.isBefore(to);
	}

	public boolean equalPaymentDate(LocalDate paymentDate) {
		if (this.paymentDate == null) {
			return false;
		}
		return this.paymentDate.equals(paymentDate);
	}

	public String basicIsoForRecordDate() {
		return basicIso(recordDate);
	}

	public String basicIsoForPaymentDate() {
		return basicIso(paymentDate);
	}

	private String basicIso(LocalDate localDate) {
		if (localDate == null) {
			return Strings.EMPTY;
		}
		return localDate.format(DateTimeFormatter.BASIC_ISO_DATE);
	}

	public boolean isSatisfiedBy(PurchaseHistory history) {
		return history.isSatisfiedDividend(exDividendDate);
	}

	public boolean isPurchaseDateBeforeExDividendDate(PurchaseHistory history) {
		return history.isPurchaseDateBeforeExDividendDate(exDividendDate.atStartOfDay());
	}
}