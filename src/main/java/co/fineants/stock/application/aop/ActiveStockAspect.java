package co.fineants.stock.application.aop;

import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import co.fineants.api.domain.portfolio.service.PortfolioService;
import co.fineants.api.domain.stock_target_price.service.StockTargetPriceService;
import co.fineants.api.domain.watchlist.service.WatchListService;
import co.fineants.stock.annotation.ActiveStockMarker;
import co.fineants.stock.event.StocksViewedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ActiveStockAspect {
	private final PortfolioService portfolioService;
	private final WatchListService watchListService;
	private final StockTargetPriceService stockTargetPriceService;
	private final ApplicationEventPublisher eventPublisher;

	private final SpelExpressionParser parser = new SpelExpressionParser();
	private final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

	@Before("@annotation(marker)")
	public void markBeforeController(JoinPoint joinPoint, ActiveStockMarker marker) {
		try {
			EvaluationContext context = getContext(joinPoint);
			Object evaluatedValue = parser.parseExpression(marker.resourceId()).getValue(context);

			if (evaluatedValue == null) {
				return;
			}
			Set<String> tickers = switch (marker.type()) {
				case MEMBER -> portfolioService.getAllPortfolioTickers((Long)evaluatedValue);
				case PORTFOLIO -> portfolioService.getTickerSymbolsInPortfolio((Long)evaluatedValue);
				case STOCK -> Set.of((String)evaluatedValue);
				case STOCK_TARGET_PRICE -> stockTargetPriceService.getAllStockTargetPriceTickers((Long)evaluatedValue);
				case WATCHLIST -> watchListService.getAllWatchListTickers((Long)evaluatedValue);
			};
			if (tickers.isEmpty()) {
				return;
			}
			eventPublisher.publishEvent(new StocksViewedEvent(tickers));
		} catch (Exception e) {
			log.error("Failed to process ActiveStockMarker for type: {}", marker.type(), e);
		}
	}

	private EvaluationContext getContext(JoinPoint joinPoint) {
		StandardEvaluationContext context = new StandardEvaluationContext();
		Object[] args = joinPoint.getArgs();
		MethodSignature signature = (MethodSignature)joinPoint.getSignature();
		String[] paramNames = discoverer.getParameterNames(signature.getMethod());

		if (paramNames != null) {
			for (int i = 0; i < paramNames.length; i++) {
				context.setVariable(paramNames[i], args[i]);
			}
		}
		return context;
	}
}
