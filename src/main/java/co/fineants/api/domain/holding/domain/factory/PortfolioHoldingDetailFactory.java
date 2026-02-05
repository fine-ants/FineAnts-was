package co.fineants.api.domain.holding.domain.factory;

import java.util.List;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingRealTimeItem;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.kis.service.ClosingPriceService;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortfolioHoldingDetailFactory {

	private final ClosingPriceService closingPriceService;
	private final PortfolioCalculator calculator;

	public List<PortfolioHoldingItem> createPortfolioHoldingItems(Portfolio portfolio) {
		return portfolio.getPortfolioHoldings().stream()
			.map(portfolioHolding -> PortfolioHoldingItem.from(
				portfolioHolding,
				getClosingPrice(portfolioHolding),
				calculator)
			)
			.toList();
	}

	private Expression getClosingPrice(PortfolioHolding portfolioHolding) {
		return closingPriceService.fetchPrice(portfolioHolding.getStock().getTickerSymbol());
	}

	public List<PortfolioHoldingRealTimeItem> createPortfolioHoldingRealTimeItems(Portfolio portfolio,
		PortfolioCalculator calculator) {
		return portfolio.getPortfolioHoldings().stream()
			.map(portfolioHolding -> PortfolioHoldingRealTimeItem.of(
				portfolioHolding,
				getClosingPrice(portfolioHolding),
				calculator
			))
			.toList();
	}
}
