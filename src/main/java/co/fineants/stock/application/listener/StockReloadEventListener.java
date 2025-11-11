package co.fineants.stock.application.listener;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import co.fineants.api.infra.s3.service.WriteDividendService;
import co.fineants.api.infra.s3.service.WriteStockService;
import co.fineants.stock.application.FindStock;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockDividend;
import co.fineants.stock.domain.event.StockReloadEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StockReloadEventListener {

	private final FindStock findStock;
	private final WriteStockService writeStockService;
	private final WriteDividendService writeDividendService;

	@TransactionalEventListener
	@Transactional(readOnly = true)
	public void on(StockReloadEvent ignoredEvent) {
		List<Stock> stocks = findStock.findAll();
		writeStockService.writeStocks(stocks);
		writeDividendService.writeDividend(toStockDividends(stocks));
	}

	private StockDividend[] toStockDividends(List<Stock> stocks) {
		return stocks.stream()
			.flatMap(stock -> stock.getStockDividends().stream())
			.toArray(StockDividend[]::new);
	}
}
