package co.fineants.api.infra.s3.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.parser.StockParser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AmazonS3FetchStockService implements FetchStockService {

	private static final String CSV_SEPARATOR_REGEX = "\\$";

	private final RemoteFileFetcher fetcher;
	private final StockParser stockParser;

	public AmazonS3FetchStockService(RemoteFileFetcher fetcher, StockParser stockParser) {
		this.fetcher = fetcher;
		this.stockParser = stockParser;
	}

	@Override
	public List<Stock> fetchStocks() {
		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(fetcher.read("local/stock/stocks.csv")))) {
			return reader.lines()
				.skip(1) // skip header line
				.map(line -> line.split(CSV_SEPARATOR_REGEX))
				.map(stockParser::parse)
				.distinct()
				.toList();
		} catch (IOException e) {
			log.error("Error reading stocks file", e);
			return new ArrayList<>();
		}
	}
}
