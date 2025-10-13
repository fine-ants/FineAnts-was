package co.fineants.api.infra.s3.service;

import java.util.List;

import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.domain.entity.StockDividend;

public interface FetchDividendService {

	List<StockDividend> fetchDividendEntityIn(List<Stock> stocks);
}
