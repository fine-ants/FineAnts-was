package codesquad.fineants.domain.stock_dividend;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.stock.Stock;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "stock_dividend", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"ticker_symbol", "recordDate"})
})
@Entity
public class StockDividend extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Long dividend;
	@Column(nullable = false)
	private LocalDate exDividendDate;
	@Column(nullable = false)
	private LocalDate recordDate;
	@Column(nullable = true)
	private LocalDate paymentDate;
	private LocalDateTime dividendMonth;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ticker_symbol", referencedColumnName = "tickerSymbol")
	private Stock stock;

	@Builder
	public StockDividend(Long id, LocalDateTime dividendMonth, LocalDate exDividendDate, LocalDate recordDate,
		LocalDate paymentDate, Long dividend, Stock stock) {
		this.id = id;
		this.dividendMonth = dividendMonth;
		this.exDividendDate = exDividendDate;
		this.recordDate = recordDate;
		this.paymentDate = paymentDate;
		this.dividend = dividend;
		this.stock = stock;
	}

	public boolean isMonthlyDividend(LocalDateTime monthDateTime) {
		return dividendMonth.getYear() == monthDateTime.getYear()
			&& dividendMonth.getMonthValue() == monthDateTime.getMonthValue();
	}
}
