package co.fineants.api.domain.holding.domain.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PortfolioStockDeletesResponse {
	private List<Long> portfolioHoldingIds;
}
