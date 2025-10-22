package co.fineants.api.infra.s3.service.imple;

import co.fineants.stock.domain.StockDividend;
import co.fineants.api.global.common.csv.CsvFormatter;
import co.fineants.api.infra.s3.service.RemoteFileUploader;
import co.fineants.api.infra.s3.service.WriteDividendService;

public class GoogleCloudStorageWriteDividendService implements WriteDividendService {

	private final CsvFormatter<StockDividend> formatter;
	private final RemoteFileUploader uploader;
	private final String dividendPath;

	public GoogleCloudStorageWriteDividendService(
		CsvFormatter<StockDividend> formatter,
		RemoteFileUploader uploader,
		String dividendPath) {
		this.formatter = formatter;
		this.uploader = uploader;
		this.dividendPath = dividendPath;
	}

	@Override
	public void writeDividend(StockDividend... dividends) {
		String content = formatter.format(dividends);
		uploader.upload(content, dividendPath);
	}
}
