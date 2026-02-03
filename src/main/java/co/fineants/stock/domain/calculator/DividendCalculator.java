package co.fineants.stock.domain.calculator;

import java.time.LocalDate;
import java.util.List;

import co.fineants.stock.domain.StockDividend;

public interface DividendCalculator {
	/**
	 * 배당이 지급되는 월 리스트를 계산후 반환한다.
	 * <p>
	 * baseDate(기준 날짜)와 같은 연도를 가진 배당금들의 월 리스트를 계산후 반환한다.
	 * 예를 들어 baseDate가 2023-06-15라면, 2023년에 지급되는 배당금들의 지급 월 리스트를 반환한다.
	 * </p>
	 * @param dividends 배당 리스트
	 * @param baseDate 기준 날짜
	 * @return 배당 지급 월 리스트
	 */
	List<Integer> calculateDividendMonths(List<StockDividend> dividends, LocalDate baseDate);
}
