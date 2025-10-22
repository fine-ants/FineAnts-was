package co.fineants.api.infra.s3.service;

import java.util.List;

import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockDividend;

public interface FetchDividendService {

	List<StockDividend> fetchDividendEntityIn(List<Stock> stocks);
}
