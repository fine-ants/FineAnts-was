package co.fineants.api.infra.s3.service.imple;

import java.util.List;

import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.global.common.csv.CsvFormatter;
import co.fineants.api.infra.s3.service.RemoteFileUploader;
import co.fineants.api.infra.s3.service.WriteStockService;

public class GoogleCloudStorageWriteStockService implements WriteStockService {

	private final CsvFormatter<Stock> formatter;
	private final RemoteFileUploader fileUploader;
	private final String filePath;

	public GoogleCloudStorageWriteStockService(CsvFormatter<Stock> formatter, RemoteFileUploader fileUploader,
		String filePath) {
		this.formatter = formatter;
		this.fileUploader = fileUploader;
		this.filePath = filePath;
	}

	@Override
	public void writeStocks(List<Stock> stocks) {
		String content = formatter.format(stocks.toArray(Stock[]::new));
		fileUploader.upload(content, filePath);
	}
}
