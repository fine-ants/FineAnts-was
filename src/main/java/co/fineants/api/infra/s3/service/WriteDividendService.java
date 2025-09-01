package co.fineants.api.infra.s3.service;

import java.util.Collection;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;

public interface WriteDividendService {
	void writeDividend(Collection<StockDividend> dividends);

	void writeDividend(StockDividend... dividends);
}
