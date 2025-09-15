package co.fineants.api.infra.s3.service.imple;

import java.util.List;

import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.global.common.csv.CsvFormatter;
import co.fineants.api.infra.s3.service.RemoteFileUploader;
import co.fineants.api.infra.s3.service.WriteStockService;

public class AmazonS3WriteStockService implements WriteStockService {

	private final RemoteFileUploader fileUploader;
	private final String filePath;
	private final CsvFormatter<Stock> formatter;

	public AmazonS3WriteStockService(
		RemoteFileUploader fileUploader,
		String filePath,
		CsvFormatter<Stock> formatter) {
		this.fileUploader = fileUploader;
		this.filePath = filePath;
		this.formatter = formatter;
	}

	@Override
	public void writeStocks(List<Stock> stocks) {
		String content = formatter.format(stocks.toArray(Stock[]::new));
		fileUploader.upload(content, filePath);
	}
}
