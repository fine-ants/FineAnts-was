package co.fineants.api.infra.s3.service.imple;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import co.fineants.api.domain.dividend.domain.parser.StockDividendCsvParser;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.domain.entity.StockDividend;
import co.fineants.api.infra.s3.service.FetchDividendService;
import co.fineants.api.infra.s3.service.RemoteFileFetcher;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoogleCloudStorageFetchDividendService implements FetchDividendService {
	private static final String CSV_SEPARATOR = ",";
	private final RemoteFileFetcher fileFetcher;
	private final String dividendPath;
	private final StockDividendCsvParser stockDividendCsvParser;

	public GoogleCloudStorageFetchDividendService(RemoteFileFetcher fileFetcher, String dividendPath,
		StockDividendCsvParser stockDividendCsvLineParser) {
		this.fileFetcher = fileFetcher;
		this.dividendPath = dividendPath;
		this.stockDividendCsvParser = stockDividendCsvLineParser;
	}

	@Override
	public List<StockDividend> fetchDividendEntityIn(List<Stock> stocks) {
		Map<String, Stock> stockMap = stocks.stream()
			.collect(Collectors.toMap(Stock::getTickerSymbol, stock -> stock));
		InputStream inputStream = fileFetcher.read(dividendPath).orElseGet(InputStream::nullInputStream);
		List<StockDividend> stockDividends = stockDividendCsvParser.parse(inputStream);
		return stockDividends.stream()
			.filter(dividend -> stockMap.containsKey(dividend.getTickerSymbol()))
			.toList();
	}
}
