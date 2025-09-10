package co.fineants.api.infra.s3.service.imple;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.dividend.domain.parser.StockDividendParser;
import co.fineants.api.domain.stock.domain.entity.Stock;
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
	private final StockDividendParser stockDividendParser;

	public AmazonS3FetchDividendService(
		RemoteFileFetcher fileFetcher,
		@Value("${aws.s3.dividend-csv-path}") String dividendPath,
		StockDividendParser stockDividendParser) {
		this.fileFetcher = fileFetcher;
		this.dividendPath = dividendPath;
		this.stockDividendParser = stockDividendParser;
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
	public List<StockDividend> fetchDividendEntityIn(List<Stock> stocks) {
		Map<String, Stock> stockMap = stocks.stream()
			.collect(Collectors.toMap(Stock::getStockCode, stock -> stock));

		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(fileFetcher.read(dividendPath).orElseThrow()))) {
			return reader.lines()
				.skip(1) // Skip header line
				.map(line -> line.split(CSV_SEPARATOR))
				.map(columns -> stockDividendParser.parseCsvLine(columns, stockMap))
				.filter(dividend -> dividend.getStock() != null)
				.distinct()
				.toList();
		} catch (Exception e) {
			log.error("Failed to read dividend file from S3", e);
			return Collections.emptyList();
		}
	}
}
