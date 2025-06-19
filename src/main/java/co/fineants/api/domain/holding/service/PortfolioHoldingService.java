package co.fineants.api.domain.holding.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.holding.domain.chart.DividendChart;
import co.fineants.api.domain.holding.domain.chart.PieChart;
import co.fineants.api.domain.holding.domain.chart.SectorChart;
import co.fineants.api.domain.holding.domain.dto.request.PortfolioHoldingCreateRequest;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioChartResponse;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioDetailRealTimeItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioDetailResponse;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioDetails;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioDividendChartItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingRealTimeItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsResponse;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioPieChartItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioSectorChartItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioStockCreateResponse;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioStockDeleteResponse;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioStockDeletesResponse;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.domain.factory.PortfolioDetailFactory;
import co.fineants.api.domain.holding.domain.factory.PortfolioHoldingDetailFactory;
import co.fineants.api.domain.holding.domain.message.PortfolioReturnsStreamMessage;
import co.fineants.api.domain.holding.domain.message.StreamMessage;
import co.fineants.api.domain.holding.event.publisher.PortfolioHoldingEventPublisher;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.portfolio.service.PortfolioCacheService;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.common.authorized.Authorized;
import co.fineants.api.global.common.authorized.service.PortfolioAuthorizedService;
import co.fineants.api.global.common.authorized.service.PortfolioHoldingAuthorizedService;
import co.fineants.api.global.common.resource.ResourceId;
import co.fineants.api.global.common.resource.ResourceIds;
import co.fineants.api.global.errors.exception.business.CashNotSufficientInvalidInputException;
import co.fineants.api.global.errors.exception.business.HoldingNotFoundException;
import co.fineants.api.global.errors.exception.business.PortfolioNotFoundException;
import co.fineants.api.global.errors.exception.business.PurchaseHistoryInvalidInputException;
import co.fineants.api.global.errors.exception.business.StockNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class PortfolioHoldingService {
	private final PortfolioRepository portfolioRepository;
	private final StockRepository stockRepository;
	private final PortfolioHoldingRepository portfolioHoldingRepository;
	private final PurchaseHistoryRepository purchaseHistoryRepository;
	private final PieChart pieChart;
	private final DividendChart dividendChart;
	private final SectorChart sectorChart;
	private final PortfolioDetailFactory portfolioDetailFactory;
	private final PortfolioHoldingDetailFactory portfolioHoldingDetailFactory;
	private final PortfolioHoldingEventPublisher publisher;
	private final PortfolioCacheService portfolioCacheService;
	private final PortfolioCalculator calculator;

	@Transactional
	@Authorized(serviceClass = PortfolioAuthorizedService.class)
	public PortfolioStockCreateResponse createPortfolioHolding(@ResourceId Long portfolioId,
		PortfolioHoldingCreateRequest request) {
		log.info("포트폴리오 종목 추가 서비스 요청 : portfolioId={}, request={}", portfolioId, request);

		// 포트폴리오 탐색
		Portfolio portfolio = findPortfolio(portfolioId);

		// 종목 탐색
		Stock stock = stockRepository.findByTickerSymbolIncludingDeleted(request.getTickerSymbol())
			.orElseThrow(() -> new StockNotFoundException(request.getTickerSymbol()));

		// 기존 포트폴리오 종목 탐색후 없으면 새로 생성함
		PortfolioHolding holding = portfolioHoldingRepository.findByPortfolioIdAndTickerSymbol(portfolioId,
				request.getTickerSymbol())
			.orElseGet(() -> PortfolioHolding.of(portfolio, stock));
		// 포트폴리오 종목 정보 업데이트
		PortfolioHolding saveHolding = portfolioHoldingRepository.save(holding);

		if (request.isPurchaseHistoryComplete()) { // 매입 이력 정보가 모두 입력된 경우
			validateCashSufficientForPurchase(request, portfolio);
			purchaseHistoryRepository.save(PurchaseHistory.of(saveHolding, request.getPurchaseHistory()));
		} else if (!request.isPurchaseHistoryAllNull()) { // 매입 이력 정보가 일부 입력된 경우
			throw new PurchaseHistoryInvalidInputException(request.toString());
		}

		// 포트폴리오의 종목 캐시 업데이트
		Set<String> cachedTickers = portfolioCacheService.updateTickerSymbolsFrom(portfolioId);
		log.debug("update cached tickerSymbols: {}", cachedTickers);

		// 포트폴리오 종목 이벤트 발행
		publisher.publishPortfolioHolding(stock.getTickerSymbol());
		log.info("포트폴리오 종목 추가 결과 : {}", saveHolding);
		return PortfolioStockCreateResponse.from(saveHolding);
	}

	@Transactional
	public void createPortfolioHolding_temp(PortfolioHolding holding) {
		portfolioHoldingRepository.save(holding);
	}

	@Transactional
	@Authorized(serviceClass = PortfolioHoldingAuthorizedService.class)
	@CacheEvict(value = "tickerSymbols", key = "#portfolioId")
	public PortfolioStockDeleteResponse deletePortfolioStock(@ResourceId Long portfolioHoldingId,
		@NotNull Long portfolioId) {
		log.info("포트폴리오 종목 삭제 서비스 : portfolioHoldingId={}, portfolioId={}", portfolioHoldingId, portfolioId);
		purchaseHistoryRepository.deleteAllByPortfolioHoldingIdIn(List.of(portfolioHoldingId));

		int deleted = portfolioHoldingRepository.deleteAllByIdIn(List.of(portfolioHoldingId));
		log.info("포트폴리오 종목 삭제 개수 : {}", deleted);
		return new PortfolioStockDeleteResponse(portfolioHoldingId);
	}

	@Transactional
	@Authorized(serviceClass = PortfolioHoldingAuthorizedService.class)
	@CacheEvict(value = "tickerSymbols", key = "#portfolioId")
	public PortfolioStockDeletesResponse deletePortfolioHoldings(Long portfolioId, Long memberId,
		@ResourceIds List<Long> portfolioHoldingIds) {
		log.info("포트폴리오 종목 다수 삭제 서비스 : portfolioId={}, memberId={}, portfolioHoldingIds={}", portfolioId, memberId,
			portfolioHoldingIds);
		validateExistPortfolioHolding(portfolioHoldingIds);

		purchaseHistoryRepository.deleteAllByPortfolioHoldingIdIn(portfolioHoldingIds);
		try {
			portfolioHoldingRepository.deleteAllByIdIn(portfolioHoldingIds);
		} catch (EmptyResultDataAccessException e) {
			throw new HoldingNotFoundException(portfolioHoldingIds.toString(), e);
		}
		return new PortfolioStockDeletesResponse(portfolioHoldingIds);
	}

	private Portfolio findPortfolio(Long portfolioId) {
		return portfolioRepository.findById(portfolioId)
			.orElseThrow(() -> new PortfolioNotFoundException(portfolioId.toString()));
	}

	private void validateCashSufficientForPurchase(PortfolioHoldingCreateRequest request, Portfolio portfolio) {
		Expression purchasedAmount = request.getPurchaseHistory().getNumShares()
			.multiply(request.getPurchaseHistory().getPurchasePricePerShare());
		if (!portfolio.isCashSufficientForPurchase(purchasedAmount, calculator)) {
			throw new CashNotSufficientInvalidInputException(request.toString());
		}
	}

	private void validateExistPortfolioHolding(List<Long> portfolioHoldingIds) {
		portfolioHoldingIds.stream()
			.filter(portfolioHoldingId -> !portfolioHoldingRepository.existsById(portfolioHoldingId))
			.forEach(portfolioHoldingId -> {
				throw new HoldingNotFoundException(portfolioHoldingId.toString());
			});
	}

	@Transactional(readOnly = true)
	@Authorized(serviceClass = PortfolioAuthorizedService.class)
	public PortfolioHoldingsResponse readPortfolioHoldings(@ResourceId Long portfolioId) {
		Portfolio portfolio = findPortfolio(portfolioId);
		PortfolioDetailResponse portfolioDetail = portfolioDetailFactory.createPortfolioDetailItem(portfolio);
		List<PortfolioHoldingItem> portfolioHoldingItems = portfolioHoldingDetailFactory.createPortfolioHoldingItems(
			portfolio);
		return PortfolioHoldingsResponse.of(portfolioDetail, portfolioHoldingItems);
	}

	private Portfolio findPortfolioUsingFetchJoin(Long portfolioId) {
		return portfolioRepository.findByPortfolioIdWithAll(portfolioId)
			.orElseThrow(() -> new PortfolioNotFoundException(portfolioId.toString()));
	}

	@Transactional(readOnly = true)
	public StreamMessage getPortfolioReturns(Long portfolioId) {
		Portfolio portfolio = findPortfolioUsingFetchJoin(portfolioId);
		PortfolioDetailRealTimeItem portfolioDetail = portfolioDetailFactory.createPortfolioDetailRealTimeItem(
			portfolio);
		List<PortfolioHoldingRealTimeItem> portfolioHoldingDetails =
			portfolioHoldingDetailFactory.createPortfolioHoldingRealTimeItems(portfolio, calculator);
		return new PortfolioReturnsStreamMessage(portfolioDetail, portfolioHoldingDetails);
	}

	@Transactional(readOnly = true)
	@Authorized(serviceClass = PortfolioAuthorizedService.class)
	public PortfolioChartResponse readPortfolioCharts(@ResourceId Long portfolioId, LocalDate currentLocalDate) {
		Portfolio portfolio = findPortfolio(portfolioId);
		PortfolioDetails portfolioDetails = PortfolioDetails.from(portfolio);
		List<PortfolioPieChartItem> pieChartItems = pieChart.createItemsBy(portfolio);
		List<PortfolioDividendChartItem> dividendChartItems = dividendChart.createItemsBy(portfolio, currentLocalDate);
		List<PortfolioSectorChartItem> sectorChartItems = sectorChart.createBy(portfolio);
		return PortfolioChartResponse.create(portfolioDetails, pieChartItems, dividendChartItems, sectorChartItems);
	}
}
