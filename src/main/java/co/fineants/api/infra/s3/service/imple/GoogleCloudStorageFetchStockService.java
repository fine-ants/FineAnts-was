package co.fineants.api.infra.s3.service.imple;

import java.io.InputStream;
import java.util.List;

import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.parser.StockCsvParser;
import co.fineants.api.domain.stock.parser.StockParser;
import co.fineants.api.infra.s3.service.FetchStockService;
import co.fineants.api.infra.s3.service.RemoteFileFetcher;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoogleCloudStorageFetchStockService implements FetchStockService {

	private static final String CSV_SEPARATOR_REGEX = "\\$";

	private final RemoteFileFetcher fetcher;
	private final String filePath;
	private final StockParser stockParser;

	public GoogleCloudStorageFetchStockService(RemoteFileFetcher fetcher, String filePath, StockParser stockParser) {
		this.fetcher = fetcher;
		this.filePath = filePath;
		this.stockParser = stockParser;
	}

	@Override
	public List<Stock> fetchStocks() {
		InputStream inputStream = fetcher.read(filePath).orElseThrow();
		return parse(inputStream);
	}

	public List<Stock> parse(InputStream inputStream) {
		return new StockCsvParser(CSV_SEPARATOR_REGEX, stockParser).parse(inputStream);
	}
}
