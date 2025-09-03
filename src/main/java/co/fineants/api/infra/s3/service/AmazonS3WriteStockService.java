package co.fineants.api.infra.s3.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import co.fineants.api.domain.stock.domain.entity.Stock;

@Service
public class AmazonS3WriteStockService implements WriteStockService {

	private final RemoteFileUploader fileUploader;
	private final String filePath;
	private final CsvFormatter<Stock> formatter;

	public AmazonS3WriteStockService(
		RemoteFileUploader fileUploader,
		@Value("${aws.s3.stock-path}") String filePath,
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
