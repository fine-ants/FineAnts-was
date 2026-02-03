package co.fineants.stock.application;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.kis.service.ClosingPriceService;
import co.fineants.api.domain.kis.service.CurrentPriceService;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.global.errors.exception.business.StockNotFoundException;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockRepository;
import co.fineants.stock.domain.calculator.PriceCalculator;
import co.fineants.stock.infrastructure.StockQueryDslRepository;
import co.fineants.stock.presentation.dto.response.StockResponse;
import co.fineants.stock.presentation.dto.response.StockSearchItem;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchStock {

	private final StockQueryDslRepository repository;
	private final StockRepository stockRepository;
	private final LocalDateTimeService localDateTimeService;
	private final CurrentPriceService currentPriceService;
	private final ClosingPriceService closingPriceService;
	private final PriceCalculator priceCalculator;

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
		LocalDate baseDate = localDateTimeService.getLocalDateWithNow();
		Stock stock = stockRepository.findByTickerSymbolIncludingDeleted(tickerSymbol)
			.orElseThrow(() -> new StockNotFoundException(tickerSymbol));
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		Money currentPrice = currentPriceService.fetchPrice(tickerSymbol);
		Money closingPrice = closingPriceService.fetchPrice(tickerSymbol);

		Money dailyChange = priceCalculator.calculateDailyChange(currentPrice, closingPrice).reduce(bank, to);
		Percentage dailyChangeRate = priceCalculator.calculateDailyChangeRate(currentPrice, closingPrice)
			.toPercentage(bank, to);
		Money annualDividend = priceCalculator.calculateAnnualDividend(stock.getStockDividends(), baseDate)
			.reduce(bank, to);
		Percentage annualDividendYield = priceCalculator.calculateAnnualDividendYield(stock.getStockDividends(),
			currentPrice, baseDate).toPercentage(bank, to);
		// TODO: 별도의 클래스로 변경
		List<Integer> dividendMonths = stock.getDividendMonths(baseDate).stream()
			.map(Month::getValue)
			.toList();
		return StockResponse.builder()
			.stockCode(stock.getStockCode())
			.tickerSymbol(stock.getTickerSymbol())
			.companyName(stock.getCompanyName())
			.companyNameEng(stock.getCompanyNameEng())
			.market(stock.getMarket())
			.currentPrice(currentPrice)
			.dailyChange(dailyChange)
			.dailyChangeRate(dailyChangeRate)
			.sector(stock.getSector())
			.annualDividend(annualDividend)
			.annualDividendYield(annualDividendYield)
			.dividendMonths(dividendMonths)
			.build();
	}
}
