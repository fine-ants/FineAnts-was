package co.fineants.api.infra.s3.service;

import java.util.List;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.infra.s3.dto.StockDividendDto;

public interface FetchDividendService {
	List<StockDividendDto> fetchDividend();

	List<StockDividend> fetchDividendEntityIn(List<Stock> stocks);
}
