package co.fineants.api.infra.s3.service;

import java.util.Collection;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.stock.domain.entity.StockDividendTemp;

public interface WriteDividendService {
	void writeDividend(Collection<StockDividend> dividends);

	void writeDividend(StockDividend... dividends);

	void writeDividendTemp(StockDividendTemp... dividends);
}
