package co.fineants.api.domain.holding.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.holding.domain.dto.request.PortfolioHoldingCreateRequest;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.service.PortfolioService;
import co.fineants.api.domain.purchasehistory.service.PurchaseHistoryService;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.service.StockService;
import co.fineants.api.global.common.authorized.Authorized;
import co.fineants.api.global.common.authorized.service.PortfolioAuthorizedService;
import co.fineants.api.global.common.resource.ResourceId;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioHoldingCreateFacade {

	private final PortfolioService portfolioService;
	private final StockService stockService;
	private final PortfolioHoldingService portfolioHoldingService;
	private final PurchaseHistoryService purchaseHistoryService;

	@Transactional
	@Authorized(serviceClass = PortfolioAuthorizedService.class)
	public PortfolioHolding create(PortfolioHoldingCreateRequest request,
		@ResourceId Long portfolioId) {
		// 포트폴리오 탐색
		Portfolio portfolio = portfolioService.findPortfolio(portfolioId);
		// 종목 탐색
		Stock stock = stockService.getStock(request.getTickerSymbol());
		// 기존 포트폴리오 종목 조회 or 생성
		PortfolioHolding holding = portfolioHoldingService.getPortfolioHoldingBy(portfolio, stock)
			.orElseGet(() -> PortfolioHolding.of(portfolio, stock));
		// 포트폴리오 종목 저장
		PortfolioHolding saveHolding = portfolioHoldingService.savePortfolioHolding(holding);
		// 매입 이력 생성 후 저장 (생성 못하면 생략)
		request.toPurchaseHistoryEntity(saveHolding)
			.ifPresent(purchaseHistory -> purchaseHistoryService.savePurchaseHistory(purchaseHistory, portfolio));
		return saveHolding;
	}
}
