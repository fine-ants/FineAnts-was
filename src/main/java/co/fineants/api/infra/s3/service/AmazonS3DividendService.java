package co.fineants.api.infra.s3.service;

import static java.nio.charset.StandardCharsets.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.dividend.domain.parser.StockDividendParser;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmazonS3DividendService {
	public static final String CSV_SEPARATOR = ",";
	private final AmazonS3 amazonS3;
	@Value("${aws.s3.bucket}")
	private String bucketName;
	@Value("${aws.s3.dividend-csv-path}")
	private String dividendPath;
	private final StockRepository stockRepository;
	private final StockDividendParser stockDividendParser;

	@Transactional(readOnly = true)
	public List<StockDividend> fetchDividends() {
		return getS3Object()
			.map(this::parseStockDividends)
			.orElseGet(Collections::emptyList);
	}

	private Optional<S3Object> getS3Object() {
		try {
			return Optional.ofNullable(amazonS3.getObject(new GetObjectRequest(bucketName, dividendPath)));
		} catch (AmazonServiceException e) {
			log.error(e.getMessage());
			return Optional.empty();
		}
	}

	private List<StockDividend> parseStockDividends(S3Object s3Object) {
		try (BufferedReader br = new BufferedReader(
			new InputStreamReader(s3Object.getObjectContent(), UTF_8))) {
			Map<String, Stock> stockMap = stockRepository.findAll().stream()
				.collect(Collectors.toMap(Stock::getStockCode, stock -> stock));
			return br.lines()
				.skip(1) // skip title
				.map(line -> line.split(CSV_SEPARATOR))
				.map(columns -> stockDividendParser.parseCsvLine(columns, stockMap))
				.distinct()
				.toList();
		} catch (Exception e) {
			log.error(e.getMessage());
			return Collections.emptyList();
		}
	}
}
