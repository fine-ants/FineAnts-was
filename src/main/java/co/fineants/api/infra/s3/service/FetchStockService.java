package co.fineants.api.infra.s3.service;

import java.util.List;

import co.fineants.stock.domain.Stock;

public interface FetchStockService {
	List<Stock> fetchStocks();
}
