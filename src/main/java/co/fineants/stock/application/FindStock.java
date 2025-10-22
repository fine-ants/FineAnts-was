package co.fineants.stock.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.global.errors.exception.business.StockNotFoundException;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindStock {

	private final StockRepository repository;

	@Transactional(readOnly = true)
	public Stock byTickerSymbol(String tickerSymbol) {
		return repository.findByTickerSymbol(tickerSymbol)
			.orElseThrow(() -> new StockNotFoundException(tickerSymbol));
	}

	@Transactional(readOnly = true)
	public List<Stock> findAll() {
		return repository.findAll();
	}
}
