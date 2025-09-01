package co.fineants.api.infra.s3.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import co.fineants.api.infra.s3.dto.StockDividendDto;

public class AmazonS3FetchDividendService implements FetchDividendService {

	private final RemoteFileFetcher fileFetcher;
	private final String dividendPath;

	public AmazonS3FetchDividendService(RemoteFileFetcher fileFetcher, String dividendPath) {
		this.fileFetcher = fileFetcher;
		this.dividendPath = dividendPath;
	}

	@Override
	public List<StockDividendDto> fetchDividend() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileFetcher.read(dividendPath)))) {
			return getStockDividendDtoList(reader);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read dividend file from S3", e);
		}
	}

	@NotNull
	public List<StockDividendDto> getStockDividendDtoList(BufferedReader reader) {
		return reader.lines()
			.skip(1) // Skip header line
			.map(line -> line.split(","))
			.map(StockDividendDto::from)
			.toList();
	}
}
