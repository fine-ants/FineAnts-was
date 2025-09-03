package co.fineants.api.infra.s3.service;

import java.util.List;

import org.apache.logging.log4j.util.Strings;

import co.fineants.api.domain.stock.domain.entity.Stock;

public class AmazonS3WriteStockService implements WriteStockService {

	private RemoteFileUploader fileUploader;
	private String filePath = "local/stock/stocks.csv";

	@Override
	public void writeStocks(List<Stock> stocks) {
		// String content = formatter.format(stocks);
		fileUploader.upload(Strings.EMPTY, filePath);
	}
}
