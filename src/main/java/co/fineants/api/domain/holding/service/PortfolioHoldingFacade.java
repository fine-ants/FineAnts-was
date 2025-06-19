package co.fineants.api.domain.holding.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.holding.domain.dto.request.PortfolioHoldingCreateRequest;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.service.PortFolioService;
import co.fineants.api.domain.purchasehistory.service.PurchaseHistoryService;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.service.StockService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioHoldingFacade {

	private final PortFolioService portFolioService;
	private final StockService stockService;
	private final PortfolioHoldingService portfolioHoldingService;

	private final PurchaseHistoryService purchaseHistoryService;

	@Transactional
	public PortfolioHolding createPortfolioHolding(PortfolioHoldingCreateRequest request, Long portfolioId) {
		// 포트폴리오 탐색
		Portfolio portfolio = portFolioService.findPortfolio(portfolioId);
		// 종목 탐색
		Stock stock = stockService.getStock(request.getTickerSymbol());
		// 포트폴리오 종목 저장
		PortfolioHolding holding = portfolioHoldingService.savePortfolioHolding(
			PortfolioHolding.of(portfolio, stock));
		// 매입 이력 생성 후 저장 (생성 못하면 생략)
		request.toPurchaseHistoryEntity(holding)
			.ifPresent(purchaseHistory -> purchaseHistoryService.savePurchaseHistory(purchaseHistory, portfolio));
		return holding;
	}
}
