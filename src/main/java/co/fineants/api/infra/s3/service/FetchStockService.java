package co.fineants.api.infra.s3.service;

import java.util.List;

import co.fineants.api.domain.stock.domain.entity.Stock;

public interface FetchStockService {
	List<Stock> fetchStocks();
}
