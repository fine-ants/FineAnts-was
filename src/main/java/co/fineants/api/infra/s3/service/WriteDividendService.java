package co.fineants.api.infra.s3.service;

import co.fineants.stock.domain.StockDividend;

public interface WriteDividendService {

	void writeDividend(StockDividend... dividends);
}
