package co.fineants.api.infra.s3.service.imple;

import java.util.Collections;
import java.util.List;

import co.fineants.stock.domain.Stock;
import co.fineants.api.domain.stock.parser.StockCsvParser;
import co.fineants.api.infra.s3.service.FetchStockService;
import co.fineants.api.infra.s3.service.RemoteFileFetcher;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoogleCloudStorageFetchStockService implements FetchStockService {

	private final RemoteFileFetcher fetcher;
	private final String filePath;
	private final StockCsvParser stockCsvParser;

	public GoogleCloudStorageFetchStockService(RemoteFileFetcher fetcher, String filePath,
		StockCsvParser stockCsvParser) {
		this.fetcher = fetcher;
		this.filePath = filePath;
		this.stockCsvParser = stockCsvParser;
	}

	@Override
	public List<Stock> fetchStocks() {
		return fetcher.read(filePath)
			.map(stockCsvParser::parse)
			.orElseGet(Collections::emptyList);
	}
}
