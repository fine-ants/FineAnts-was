package co.fineants.api.infra.s3.service;

import co.fineants.api.domain.stock.domain.entity.StockDividendTemp;

public interface WriteDividendService {

	void writeDividendTemp(StockDividendTemp... dividends);
}
