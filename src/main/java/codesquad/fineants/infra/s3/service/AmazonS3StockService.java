package codesquad.fineants.infra.s3.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import codesquad.fineants.domain.stock.domain.entity.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AmazonS3StockService {

	public static final String CSV_SEPARATOR = ",";

	private final AmazonS3 amazonS3;
	private final String bucketName = "fineants2024";
	private final String stockPath = "local/stock/stocks.csv";

	public List<Stock> fetchStocks() {
		S3Object s3Object;
		try {
			s3Object = amazonS3.getObject(new GetObjectRequest(bucketName, stockPath));
		} catch (AmazonServiceException e) {
			log.error(e.getMessage());
			return Collections.emptyList();
		}

		try (BufferedReader br = new BufferedReader(
			new InputStreamReader(s3Object.getObjectContent(), StandardCharsets.UTF_8))) {
			return br.lines()
				.skip(1) // skip title
				.map(line -> line.split(CSV_SEPARATOR))
				.map(Stock::parse)
				.distinct()
				.toList();
		} catch (Exception e) {
			log.error(e.getMessage());
			return Collections.emptyList();
		}
	}
}
