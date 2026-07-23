package co.fineants.api.domain.holding.domain.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PortfolioStockDeletesResponse {
	private List<Long> portfolioHoldingIds;
}
