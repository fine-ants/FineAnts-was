package co.fineants.api.domain.holding.service.market_status_checker;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class KoreanMarketStatusChecker implements MarketStatusChecker {

	private final List<MarketStatusCheckerRule> rules;

	public KoreanMarketStatusChecker(MarketStatusCheckerRule... rules) {
		this.rules = Arrays.asList(rules);
	}

	@Override
	public boolean isOpen(LocalDateTime dateTime) {
		return rules.stream()
			.allMatch(rule -> rule.isOpen(dateTime));
	}
}
