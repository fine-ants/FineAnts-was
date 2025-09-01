package co.fineants.api.infra.s3.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;

public class AmazonS3FetchDividendService implements FetchDividendService {

	private final RemoteFileFetcher fileFetcher;
	private final String dividendPath;

	public AmazonS3FetchDividendService(RemoteFileFetcher fileFetcher, String dividendPath) {
		this.fileFetcher = fileFetcher;
		this.dividendPath = dividendPath;
	}

	@Override
	public List<StockDividend> fetchDividend() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileFetcher.read(dividendPath)))) {
			return reader.lines()
				.skip(1) // Skip header line
				.map(StockDividend::fromCsv)
				.toList();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read dividend file from S3", e);
		}
	}
}
