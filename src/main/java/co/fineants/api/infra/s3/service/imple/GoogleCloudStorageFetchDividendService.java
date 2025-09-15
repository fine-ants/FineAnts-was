package co.fineants.api.infra.s3.service.imple;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.dividend.domain.parser.StockDividendCsvParser;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.infra.s3.dto.StockDividendDto;
import co.fineants.api.infra.s3.service.FetchDividendService;
import co.fineants.api.infra.s3.service.RemoteFileFetcher;
import jakarta.validation.constraints.NotNull;
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
	public List<StockDividendDto> fetchDividend() {
		return fileFetcher.read(dividendPath)
			.map(this::parse)
			.orElseGet(Collections::emptyList);
	}

	@NotNull
	private List<StockDividendDto> parse(InputStream inputStream) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			return reader.lines()
				.skip(1) // Skip header line
				.map(line -> line.split(CSV_SEPARATOR))
				.map(StockDividendDto::from)
				.toList();
		} catch (Exception e) {
			log.warn("Error reading dividend file", e);
			return Collections.emptyList();
		}
	}

	@Override
	public List<StockDividend> fetchDividendEntityIn(List<Stock> stocks) {
		Map<String, Stock> stockMap = stocks.stream()
			.collect(Collectors.toMap(Stock::getStockCode, stock -> stock));
		return stockDividendCsvParser.parse(fileFetcher.read(dividendPath).orElseThrow(), stockMap);
	}
}
