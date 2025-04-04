package co.fineants.api.domain.portfolio.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.global.errors.exception.temp.PortfolioNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PortfolioCacheService {
	private final PortfolioRepository portfolioRepository;

	@Cacheable(value = "tickerSymbols", key = "#portfolioId")
	@NotNull
	public Set<String> getTickerSymbolsFromPortfolioBy(Long portfolioId) {
		Set<String> result = getTickers(portfolioId);
		log.debug("tickerSymbols: {}", result);
		return result;
	}

	@NotNull
	private Set<String> getTickers(Long portfolioId) {
		Portfolio portfolio = portfolioRepository.findByPortfolioIdWithAll(portfolioId)
			.orElseThrow(() -> new PortfolioNotFoundException(portfolioId.toString()));
		return portfolio.getPortfolioHoldings().stream()
			.map(PortfolioHolding::getStock)
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toUnmodifiableSet());
	}

	@CachePut(value = "tickerSymbols", key = "#portfolioId")
	public Set<String> updateTickerSymbolsFrom(Long portfolioId) {
		return getTickers(portfolioId);
	}
}
