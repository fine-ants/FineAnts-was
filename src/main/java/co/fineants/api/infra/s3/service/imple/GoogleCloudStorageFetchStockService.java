package co.fineants.api.infra.s3.service.imple;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import co.fineants.api.domain.stock.domain.entity.Stock;
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
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(fetcher.read(filePath).orElseThrow()))) {
			return reader.lines()
				.skip(1) // skip header line
				.map(line -> line.split(CSV_SEPARATOR_REGEX))
				.map(stockParser::parse)
				.distinct()
				.toList();
		} catch (Exception e) {
			log.warn("Error reading stocks file", e);
			return new ArrayList<>();
		}
	}
}
