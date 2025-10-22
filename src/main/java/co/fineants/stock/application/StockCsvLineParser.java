package co.fineants.stock.application;

import java.util.Arrays;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import co.fineants.stock.domain.Market;
import co.fineants.stock.domain.Stock;

@Component
public class StockCsvLineParser {

	private final String tickerSymbolPrefix;

	public StockCsvLineParser(@Value("${csv.stock.tickerSymbolPrefix}") String tickerSymbolPrefix) {
		this.tickerSymbolPrefix = tickerSymbolPrefix;
	}

	/**
	 * 종목 데이터를 파싱한다
	 * tickerSymbol 열의 데이터는 앞에 접두사로 'TS'가 붙는다
	 * Stock 인스턴스 생성시 tickerSymbol에 접두사를 제거하고 저장한다
	 * ex) 'TS005930' -> '005930'
	 *
	 * @param data the data
	 * @return the stock
	 */
	public Stock parse(String[] data) {
		try {
			String stockCode = data[0];
			String tickerSymbol = data[1].replace(tickerSymbolPrefix, Strings.EMPTY);
			String companyName = data[2];
			String companyNameEng = data[3];
			String sector = data[4];
			Market market = Market.ofMarket(data[5]);
			return Stock.of(tickerSymbol, companyName, companyNameEng, stockCode, sector, market);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException("out of index, data:" + Arrays.toString(data));
		}
	}
}
