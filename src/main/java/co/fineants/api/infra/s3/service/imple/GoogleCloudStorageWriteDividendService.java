package co.fineants.api.infra.s3.service.imple;

import java.util.Collection;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.infra.s3.service.WriteDividendService;

public class GoogleCloudStorageWriteDividendService implements WriteDividendService {
	@Override
	public void writeDividend(Collection<StockDividend> dividends) {

	}

	@Override
	public void writeDividend(StockDividend... dividends) {

	}
}
