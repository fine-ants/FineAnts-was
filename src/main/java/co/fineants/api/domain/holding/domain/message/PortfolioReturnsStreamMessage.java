package co.fineants.api.domain.holding.domain.message;

import java.util.List;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioDetailRealTimeItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingRealTimeItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;

public class PortfolioReturnsStreamMessage implements PortfolioStreamMessage {
	private final PortfolioDetailRealTimeItem portfolioDetails;
	private final List<PortfolioHoldingRealTimeItem> portfolioHoldings;

	public PortfolioReturnsStreamMessage(PortfolioDetailRealTimeItem portfolioDetails,
		List<PortfolioHoldingRealTimeItem> portfolioHoldings) {
		this.portfolioDetails = portfolioDetails;
		this.portfolioHoldings = portfolioHoldings;
	}

	@Override
	public Object getData() {
		return PortfolioHoldingsRealTimeResponse.of(
			portfolioDetails,
			portfolioHoldings
		);
	}

	@Override
	public String getEventName() {
		return "portfolioDetails";
	}
}
