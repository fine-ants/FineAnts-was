package co.fineants.api.infra.s3.service.imple;

import java.util.Collection;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.stock.domain.entity.StockDividendTemp;
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
	public void writeDividend(Collection<StockDividend> dividends) {
		writeDividend(dividends.toArray(StockDividend[]::new));
	}

	@Override
	public void writeDividend(StockDividend... dividends) {
		String content = formatter.format(dividends);
		uploader.upload(content, dividendPath);
	}

	@Override
	public void writeDividendTemp(StockDividendTemp... dividends) {
		// TODO: implement writeDividendTemp
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
