package co.fineants.api.infra.s3.service.imple;

import java.util.List;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.infra.s3.dto.StockDividendDto;
import co.fineants.api.infra.s3.service.FetchDividendService;

public class GoogleCloudStorageFetchDividendService implements FetchDividendService {
	@Override
	public List<StockDividendDto> fetchDividend() {
		return null;
	}

	@Override
	public List<StockDividend> fetchDividendEntityIn(List<Stock> stocks) {
		return null;
	}
}
