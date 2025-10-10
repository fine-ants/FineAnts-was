package co.fineants.api.infra.s3.service.imple;

import co.fineants.api.domain.stock.domain.entity.StockDividendTemp;
import co.fineants.api.global.common.csv.CsvFormatter;
import co.fineants.api.infra.s3.service.RemoteFileUploader;
import co.fineants.api.infra.s3.service.WriteDividendService;

public class AmazonS3WriteDividendService implements WriteDividendService {

	private final CsvFormatter<StockDividendTemp> formatter;
	private final RemoteFileUploader fileUploader;
	private final String filePath;

	public AmazonS3WriteDividendService(
		CsvFormatter<StockDividendTemp> formatter,
		RemoteFileUploader fileUploader,
		String filePath) {
		this.formatter = formatter;
		this.fileUploader = fileUploader;
		this.filePath = filePath;
	}

	@Override
	public void writeDividendTemp(StockDividendTemp... dividends) {
		String content = formatter.format(dividends);
		fileUploader.upload(content, filePath);
	}
}
