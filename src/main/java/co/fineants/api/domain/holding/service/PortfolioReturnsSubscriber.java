package co.fineants.api.domain.holding.service;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;

public interface PortfolioReturnsSubscriber {
	void accept(PortfolioHoldingsRealTimeResponse data);
}
