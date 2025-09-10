package co.fineants.api.infra.s3.service.imple;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.infra.s3.dto.StockDividendDto;
import co.fineants.api.infra.s3.service.FetchDividendService;
import co.fineants.api.infra.s3.service.RemoteFileFetcher;
import jakarta.validation.constraints.NotNull;

public class GoogleCloudStorageFetchDividendService implements FetchDividendService {

	private final RemoteFileFetcher fileFetcher;

	private final String dividendPath;

	public GoogleCloudStorageFetchDividendService(RemoteFileFetcher fileFetcher, String dividendPath) {
		this.fileFetcher = fileFetcher;
		this.dividendPath = dividendPath;
	}

	@Override
	public List<StockDividendDto> fetchDividend() {
		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(fileFetcher.read(dividendPath).orElseThrow()))) {
			return getStockDividendDtoList(reader);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to read dividend file from Google Storage", e);
		}
	}

	@NotNull
	private List<StockDividendDto> getStockDividendDtoList(BufferedReader reader) {
		return reader.lines()
			.skip(1) // Skip header line
			.map(line -> line.split(","))
			.map(StockDividendDto::from)
			.toList();
	}

	@Override
	public List<StockDividend> fetchDividendEntityIn(List<Stock> stocks) {
		return Collections.emptyList();
	}
}
