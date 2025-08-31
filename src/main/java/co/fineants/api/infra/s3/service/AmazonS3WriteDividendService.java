package co.fineants.api.infra.s3.service;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;

public class AmazonS3WriteDividendService implements WriteDividendService {

	private final DividendCsvFormatter formatter;

	public AmazonS3WriteDividendService(DividendCsvFormatter formatter) {
		this.formatter = formatter;
	}

	@Override
	public void writeDividend(StockDividend... dividends) {
		String content = formatter.format(dividends);
	}
}
