package co.fineants.api.infra.s3.service.imple;

import java.io.InputStream;
import java.util.List;

import co.fineants.stock.application.StockCsvParser;
import co.fineants.api.infra.s3.service.FetchStockService;
import co.fineants.api.infra.s3.service.RemoteFileFetcher;
import co.fineants.stock.domain.Stock;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AmazonS3FetchStockService implements FetchStockService {
	private final RemoteFileFetcher fetcher;
	private final String filePath;
	private final StockCsvParser stockCsvParser;

	public AmazonS3FetchStockService(
		RemoteFileFetcher fetcher,
		String filePath,
		StockCsvParser stockCsvParser) {
		this.fetcher = fetcher;
		this.filePath = filePath;
		this.stockCsvParser = stockCsvParser;
	}

	@Override
	public List<Stock> fetchStocks() {
		InputStream inputStream = fetcher.read(filePath).orElseThrow();
		return stockCsvParser.parse(inputStream);
	}
}
