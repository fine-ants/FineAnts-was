package co.fineants.api.infra.s3.service;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;

public interface WriteDividendService {
	void writeDividend(StockDividend... dividends);
}
