package co.fineants.api.domain.kis.domain.dto.response;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.dividend.domain.calculator.ExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.entity.DividendDates;
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.domain.entity.StockDividendTemp;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@JsonDeserialize(using = KisDividend.KissDividendDeserializer.class)
@EqualsAndHashCode
public class KisDividend implements Comparable<KisDividend> {
	private String tickerSymbol;
	private Money dividend;
	private LocalDate recordDate;
	private LocalDate paymentDate;

	public static KisDividend create(String tickerSymbol, Money dividend, LocalDate recordDate, LocalDate paymentDate) {
		return new KisDividend(tickerSymbol, dividend, recordDate, paymentDate);
	}

	public StockDividend toEntity(Stock stock, ExDividendDateCalculator exDividendDateCalculator) {
		LocalDate exDividendDate = exDividendDateCalculator.calculate(recordDate);
		return StockDividend.create(dividend, recordDate, exDividendDate, paymentDate, stock);
	}

	public StockDividend toEntity(Long id, Stock stock, ExDividendDateCalculator exDividendDateCalculator) {
		LocalDate exDividendDate = exDividendDateCalculator.calculate(recordDate);
		return StockDividend.create(id, dividend, recordDate, exDividendDate, paymentDate, stock);
	}

	public StockDividendTemp toEntity(ExDividendDateCalculator exDividendDateCalculator) {
		LocalDate exDividendDate = exDividendDateCalculator.calculate(recordDate);
		DividendDates dividendDates = DividendDates.of(recordDate, exDividendDate, paymentDate);
		return new StockDividendTemp(
			dividend,
			dividendDates,
			false,
			tickerSymbol
		);
	}

	public boolean containsFrom(Map<String, Stock> stockMap) {
		return stockMap.containsKey(tickerSymbol);
	}

	public boolean matchTickerSymbolAndRecordDateFrom(Map<String, Stock> stockMap) {
		if (!stockMap.containsKey(tickerSymbol)) {
			return false;
		}
		return stockMap.get(tickerSymbol).matchByTickerSymbolAndRecordDate(tickerSymbol, recordDate);
	}

	public Optional<StockDividendTemp> getStockDividendByTickerSymbolAndRecordDateFrom(Map<String, Stock> stockMap) {
		if (!stockMap.containsKey(tickerSymbol)) {
			return Optional.empty();
		}
		return stockMap.get(tickerSymbol).getStockDividendBy(tickerSymbol, recordDate);
	}

	/**
	 * 1차 정렬 : tickerSymbol asc
	 * 2차 정렬 : recordDate asc
	 * @param dividend the object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is less than,
	 * equal to, or greater than the specified object.
	 */
	@Override
	public int compareTo(@NotNull KisDividend dividend) {
		return this.tickerSymbol.compareTo(dividend.tickerSymbol) == 0
			? this.recordDate.compareTo(dividend.recordDate) : this.tickerSymbol.compareTo(dividend.tickerSymbol);
	}

	public Stock getStockBy(Map<String, Stock> stockMap) {
		if (!stockMap.containsKey(tickerSymbol)) {
			return null;
		}
		return stockMap.get(tickerSymbol);
	}

	static class KissDividendDeserializer extends JsonDeserializer<KisDividend> {

		private static final DateTimeFormatter RECORD_DATE_DTF = DateTimeFormatter.BASIC_ISO_DATE;
		private static final DateTimeFormatter OTHER_DATE_DTF = DateTimeFormatter.ofPattern("yyyy/MM/dd");

		@Override
		public KisDividend deserialize(JsonParser parser, DeserializationContext context) throws
			IOException {
			TreeNode rootNode = parser.readValueAsTree();
			KisDividend kisDividend = new KisDividend();

			JsonNode outputNode = (JsonNode)rootNode;
			JsonNode tickerSymbol = outputNode.get("sht_cd"); // 티커 심볼
			if (tickerSymbol != null) {
				kisDividend.tickerSymbol = tickerSymbol.asText();
			}

			JsonNode dividend = outputNode.get("per_sto_divi_amt"); // 배당금
			if (dividend != null) {
				kisDividend.dividend = Money.won(dividend.asLong());
			}

			JsonNode recordDate = outputNode.get("record_date"); // 배정기준일
			if (recordDate != null) {
				kisDividend.recordDate = LocalDate.parse(recordDate.asText(), RECORD_DATE_DTF);
			}

			JsonNode paymentDate = outputNode.get("divi_pay_dt"); // 현금 지급일
			if (paymentDate != null && !paymentDate.asText().isBlank()) {
				kisDividend.paymentDate = LocalDate.parse(paymentDate.asText(), OTHER_DATE_DTF);
			}
			return kisDividend;
		}
	}
}
