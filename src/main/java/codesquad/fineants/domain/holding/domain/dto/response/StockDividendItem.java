package codesquad.fineants.domain.holding.domain.dto.response;

import java.time.LocalDateTime;

import codesquad.fineants.domain.common.money.Money;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StockDividendItem {
	private Long dividendId;
	private LocalDateTime dividendMonth;
	private Money dividendAmount;
}
