package co.fineants.api.domain.holding.domain.factory;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.gainhistory.repository.PortfolioGainHistoryRepository;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioDetailRealTimeItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioDetailResponse;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.global.common.time.LocalDateTimeService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PortfolioDetailFactory {

	private final PortfolioGainHistoryRepository portfolioGainHistoryRepository;
	private final LocalDateTimeService localDateTimeService;
	private final PortfolioCalculator calculator;

	public PortfolioDetailResponse createPortfolioDetailItem(Portfolio portfolio) {
		PortfolioGainHistory history =
			portfolioGainHistoryRepository.findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc(
					portfolio.getId(), LocalDateTime.now())
				.stream()
				.findFirst()
				.orElseGet(() -> PortfolioGainHistory.empty(portfolio));
		return PortfolioDetailResponse.of(portfolio, history, localDateTimeService, calculator);
	}

	public PortfolioDetailRealTimeItem createPortfolioDetailRealTimeItem(Portfolio portfolio) {
		PortfolioGainHistory history =
			portfolioGainHistoryRepository.findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc(
					portfolio.getId(), LocalDateTime.now())
				.stream()
				.findFirst()
				.orElseGet(() -> PortfolioGainHistory.empty(portfolio));
		return PortfolioDetailRealTimeItem.of(portfolio, history, calculator);
	}
}
