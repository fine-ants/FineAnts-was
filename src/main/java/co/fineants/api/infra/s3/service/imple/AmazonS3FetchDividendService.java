package co.fineants.api.infra.s3.service.imple;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;

import co.fineants.api.domain.dividend.domain.parser.StockDividendCsvParser;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.domain.entity.StockDividendTemp;
import co.fineants.api.infra.s3.dto.StockDividendDto;
import co.fineants.api.infra.s3.service.FetchDividendService;
import co.fineants.api.infra.s3.service.RemoteFileFetcher;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AmazonS3FetchDividendService implements FetchDividendService {

	private static final String CSV_SEPARATOR = ",";
	private final RemoteFileFetcher fileFetcher;
	private final String dividendPath;
	private final StockDividendCsvParser stockDividendCsvParser;

	public AmazonS3FetchDividendService(
		RemoteFileFetcher fileFetcher,
		@Value("${aws.s3.dividend-csv-path}") String dividendPath,
		StockDividendCsvParser stockDividendCsvParser) {
		this.fileFetcher = fileFetcher;
		this.dividendPath = dividendPath;
		this.stockDividendCsvParser = stockDividendCsvParser;
	}

	@Override
	public List<StockDividendDto> fetchDividend() {
		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(fileFetcher.read(dividendPath).orElseThrow()))) {
			return getStockDividendDtoList(reader);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to read dividend file from S3", e);
		}
	}

	@NotNull
	private List<StockDividendDto> getStockDividendDtoList(BufferedReader reader) {
		return reader.lines()
			.skip(1) // Skip header line
			.map(line -> line.split(CSV_SEPARATOR))
			.map(StockDividendDto::from)
			.toList();
	}

	@Override
	public List<StockDividendTemp> fetchDividendEntityIn(List<Stock> stocks) {
		Map<String, Stock> stockMap = stocks.stream()
			.collect(Collectors.toMap(Stock::getTickerSymbol, stock -> stock));
		List<StockDividendTemp> stockDividends = stockDividendCsvParser.parse(
			fileFetcher.read(dividendPath).orElseThrow());
		return stockDividends.stream()
			.filter(stockDividend -> stockMap.containsKey(stockDividend.getTickerSymbol()))
			.toList();
	}
}
