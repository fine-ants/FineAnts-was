package co.fineants.api.domain.dividend.domain.entity;

import co.fineants.api.domain.BaseEntity;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.MoneyConverter;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.global.common.csv.CsvLineConvertible;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString(exclude = {"stock"})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"dividendDates", "stock"}, callSuper = false)
@Table(name = "stock_dividend", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"ticker_symbol", "record_date"})
})
@Entity
public class StockDividend extends BaseEntity implements CsvLineConvertible {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Convert(converter = MoneyConverter.class)
	@Column(precision = 19, nullable = false)
	private Money dividend;

	@Embedded
	private DividendDates dividendDates;

	@Column(name = "is_deleted", nullable = false)
	private boolean isDeleted;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ticker_symbol")
	private Stock stock;
	
	@Override
	public String toCsvLine() {
		return String.join(",",
			this.id.toString(),
			this.dividend.toRawAmount(),
			dividendDates.basicIsoForRecordDate(),
			dividendDates.basicIsoForPaymentDate(),
			this.stock.getStockCode());
	}
}
