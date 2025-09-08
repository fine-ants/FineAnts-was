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
public class AmazonS3FetchStockService implements FetchStockService {

	private static final String CSV_SEPARATOR_REGEX = "\\$";

	private final RemoteFileFetcher fetcher;
	private final StockParser stockParser;
	private final String filePath;

	public AmazonS3FetchStockService(
		RemoteFileFetcher fetcher,
		StockParser stockParser,
		String filePath) {
		this.fetcher = fetcher;
		this.stockParser = stockParser;
		this.filePath = filePath;
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
			log.error("Error reading stocks file", e);
			return new ArrayList<>();
		}
	}
}
