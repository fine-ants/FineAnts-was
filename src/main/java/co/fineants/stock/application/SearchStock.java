package co.fineants.stock.application;

import java.time.Month;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.PriceRepository;
import co.fineants.api.domain.kis.service.CurrentPriceService;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.global.errors.exception.business.StockNotFoundException;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockRepository;
import co.fineants.stock.infrastructure.StockQueryDslRepository;
import co.fineants.stock.presentation.dto.response.StockResponse;
import co.fineants.stock.presentation.dto.response.StockSearchItem;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchStock {

	private final StockQueryDslRepository repository;
	private final StockRepository stockRepository;
	private final PriceRepository priceRepository;
	private final ClosingPriceRepository closingPriceRepository;
	private final LocalDateTimeService localDateTimeService;
	private final CurrentPriceService currentPriceService;

	@Transactional(readOnly = true)
	public List<StockSearchItem> search(String keyword) {
		return repository.getStock(keyword).stream()
			.map(StockSearchItem::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<StockSearchItem> search(String tickerSymbol, int size, String keyword) {
		return repository.getSliceOfStock(tickerSymbol, size, keyword).stream()
			.map(StockSearchItem::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public StockResponse findDetailedStock(String tickerSymbol) {
		Stock stock = stockRepository.findByTickerSymbolIncludingDeleted(tickerSymbol)
			.orElseThrow(() -> new StockNotFoundException(tickerSymbol));
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		Money currentPrice = currentPriceService.fetchPrice(tickerSymbol);
		return StockResponse.builder()
			.stockCode(stock.getStockCode())
			.tickerSymbol(stock.getTickerSymbol())
			.companyName(stock.getCompanyName())
			.companyNameEng(stock.getCompanyNameEng())
			.market(stock.getMarket())
			.currentPrice(currentPrice.reduce(bank, to))
			.dailyChange(stock.getDailyChange(priceRepository, closingPriceRepository).reduce(bank, to))
			.dailyChangeRate(stock.getDailyChangeRate(priceRepository, closingPriceRepository).toPercentage(
				bank, to))
			.sector(stock.getSector())
			.annualDividend(stock.getAnnualDividend(localDateTimeService).reduce(bank, to))
			.annualDividendYield(
				stock.getAnnualDividendYield(priceRepository, localDateTimeService).toPercentage(bank, to))
			.dividendMonths(stock.getDividendMonths(localDateTimeService).stream()
				.map(Month::getValue)
				.toList())
			.build();
	}
}
