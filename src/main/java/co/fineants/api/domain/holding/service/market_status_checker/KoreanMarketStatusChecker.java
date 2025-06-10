package co.fineants.api.domain.holding.service.market_status_checker;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class KoreanMarketStatusChecker implements MarketStatusChecker {

	private final List<MarketStatusCheckerRule> rules;

	/**
	 * KoreanMarketStatusChecker 생성자
	 * @param rules 시장 상태를 확인하기 위한 규칙 배열
	 * @throws IllegalArgumentException rules 배열이 null이거나 비어있으면 IllegalArgumentException을 발생시킴
	 */
	public KoreanMarketStatusChecker(MarketStatusCheckerRule... rules) throws IllegalArgumentException {
		if (rules == null || rules.length == 0) {
			throw new IllegalArgumentException("At least one rule must be provided");
		}
		this.rules = Arrays.asList(rules);
	}

	@Override
	public boolean isOpen(LocalDateTime dateTime) {
		return rules.stream()
			.allMatch(rule -> rule.isOpen(dateTime));
	}

	@Override
	public boolean isClosed(LocalDateTime dateTime) {
		return rules.stream().anyMatch(rule -> rule.isClose(dateTime));
	}
}
