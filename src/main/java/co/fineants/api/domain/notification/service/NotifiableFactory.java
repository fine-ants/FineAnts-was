package co.fineants.api.domain.notification.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.common.notification.PortfolioMaximumLossNotifiable;
import co.fineants.api.domain.common.notification.PortfolioTargetGainNotifiable;
import co.fineants.api.domain.common.notification.TargetPriceNotificationNotifiable;
import co.fineants.api.domain.kis.service.CurrentPriceService;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
import co.fineants.api.domain.stock_target_price.domain.entity.TargetPriceNotification;
import co.fineants.api.domain.stock_target_price.repository.StockTargetPriceRepository;
import co.fineants.api.global.errors.exception.business.PortfolioNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotifiableFactory {

	private final PortfolioRepository portfolioRepository;
	private final StockTargetPriceRepository stockTargetPriceRepository;
	private final CurrentPriceService currentPriceService;

	@Transactional(readOnly = true)
	public List<Notifiable> getAllPortfolioTargetGainNotifiable(Predicate<Portfolio> reachedPredicate) {
		return portfolioRepository.findAllWithAll().stream()
			.map(mapToTargetGainNotifiable(reachedPredicate))
			.map(Notifiable.class::cast)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<Notifiable> getAllPortfolioMaximumLossNotifiable(Predicate<Portfolio> reachedPredicate) {
		return portfolioRepository.findAllWithAll().stream()
			.map(mapToMaximumLossNotifiable(reachedPredicate))
			.map(Notifiable.class::cast)
			.toList();
	}

	@Transactional(readOnly = true)
	public Notifiable getPortfolioTargetGainNotifiable(Long portfolioId, Predicate<Portfolio> reachedPredicate) {
		return portfolioRepository.findByPortfolioIdWithAll(portfolioId)
			.map(mapToTargetGainNotifiable(reachedPredicate))
			.map(Notifiable.class::cast)
			.orElseThrow(() -> new PortfolioNotFoundException(portfolioId.toString()));
	}

	private Function<Portfolio, Notifiable> mapToTargetGainNotifiable(Predicate<Portfolio> reachedPredicate) {
		return portfolio -> {
			boolean isReached = reachedPredicate.test(portfolio);
			return PortfolioTargetGainNotifiable.from(portfolio, isReached);
		};
	}

	@Transactional(readOnly = true)
	public Notifiable getPortfolioMaximumLossNotifiable(Long portfolioId, Predicate<Portfolio> reachedPredicate) {
		return portfolioRepository.findByPortfolioIdWithAll(portfolioId)
			.map(mapToMaximumLossNotifiable(reachedPredicate))
			.map(Notifiable.class::cast)
			.orElseThrow(() -> new PortfolioNotFoundException(portfolioId.toString()));
	}

	private Function<Portfolio, Notifiable> mapToMaximumLossNotifiable(Predicate<Portfolio> reachedPredicate) {
		return portfolio -> {
			boolean isReached = reachedPredicate.test(portfolio);
			return PortfolioMaximumLossNotifiable.from(portfolio, isReached);
		};
	}

	@Transactional(readOnly = true)
	public List<Notifiable> getAllTargetPriceNotificationsBy(Long memberId) {
		return stockTargetPriceRepository.findAllByMemberId(memberId)
			.stream()
			.map(StockTargetPrice::getTargetPriceNotifications)
			.flatMap(Collection::stream)
			.sorted(Comparator.comparingLong(TargetPriceNotification::getId))
			.map(targetPriceNotification -> {
				boolean isReached = isReached(targetPriceNotification);
				return TargetPriceNotificationNotifiable.from(targetPriceNotification, isReached);
			})
			.map(Notifiable.class::cast)
			.toList();
	}

	private boolean isReached(TargetPriceNotification targetPriceNotification) {
		String tickerSymbol = targetPriceNotification.getStockTargetPrice().getStock().getTickerSymbol();
		Money currentPrice = currentPriceService.fetchPrice(tickerSymbol);
		return targetPriceNotification.getTargetPrice().compareTo(currentPrice) == 0;
	}

	@Transactional(readOnly = true)
	public List<Notifiable> getAllTargetPriceNotificationsBy(List<String> tickerSymbols) {
		return stockTargetPriceRepository.findAllByTickerSymbols(
				tickerSymbols)
			.stream()
			.map(StockTargetPrice::getTargetPriceNotifications)
			.flatMap(Collection::stream)
			.map(targetPriceNotification -> {
				boolean isReached = isReached(targetPriceNotification);
				return TargetPriceNotificationNotifiable.from(targetPriceNotification, isReached);
			})
			.map(Notifiable.class::cast)
			.toList();
	}
}
