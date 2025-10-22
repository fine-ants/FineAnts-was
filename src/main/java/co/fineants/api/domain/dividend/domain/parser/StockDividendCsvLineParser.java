package co.fineants.api.domain.dividend.domain.parser;

import static co.fineants.stock.domain.StockDividend.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.dividend.domain.calculator.ExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.entity.DividendDates;
import co.fineants.stock.domain.StockDividend;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StockDividendCsvLineParser {

	private final ExDividendDateCalculator calculator;

	public StockDividend parseCsvLine(String[] data) {
		String tickerSymbol = data[0].replace(TICKER_PREFIX, Strings.EMPTY);
		Money dividend = Money.won(Long.parseLong(data[1]));
		LocalDate recordDate = basicIso(data[2]);
		LocalDate exDividendDate = calculator.calculate(recordDate);
		LocalDate paymentDate = basicIso(data[3]);
		DividendDates dividendDates = DividendDates.of(recordDate, exDividendDate, paymentDate);
		boolean isDeleted = Boolean.parseBoolean(data[4]);
		return new StockDividend(
			dividend,
			dividendDates,
			isDeleted,
			tickerSymbol
		);
	}

	private LocalDate basicIso(String localDateString) {
		if (Strings.isBlank(localDateString)) {
			return null;
		}
		return LocalDate.parse(localDateString, DateTimeFormatter.BASIC_ISO_DATE);
	}
}
