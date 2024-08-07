package codesquad.fineants.domain.portfolio.domain.dto.response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import codesquad.fineants.domain.gainhistory.domain.entity.PortfolioGainHistory;
import codesquad.fineants.domain.kis.repository.CurrentPriceRepository;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class PortfoliosResponse {
	private List<PortFolioItem> portfolios;

	public static PortfoliosResponse of(List<Portfolio> portfolios,
		Map<Portfolio, PortfolioGainHistory> portfolioGainHistoryMap,
		CurrentPriceRepository manager) {
		return new PortfoliosResponse(getContents(portfolios, portfolioGainHistoryMap, manager));
	}

	private static List<PortFolioItem> getContents(List<Portfolio> portfolios,
		Map<Portfolio, PortfolioGainHistory> portfolioGainHistoryMap, CurrentPriceRepository manager) {
		return portfolios.stream()
			.map(portfolio -> {
				portfolio.applyCurrentPriceAllHoldingsBy(manager);
				return PortFolioItem.of(portfolio, portfolioGainHistoryMap.get(portfolio));
			})
			.collect(Collectors.toList());
	}
}
