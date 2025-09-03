package co.fineants.api.infra.s3.service;

import static co.fineants.api.domain.stock.service.StockCsvReader.*;
import static java.nio.charset.StandardCharsets.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

import co.fineants.api.domain.stock.domain.entity.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AmazonS3StockService {

	private final AmazonS3 amazonS3;
	@Value("${aws.s3.bucket}")
	private String bucketName;
	@Value("${aws.s3.stock-path}")
	private String stockPath;

	// todo: extract to interface
	public void writeStocks(List<Stock> stocks) {
		String title = csvTitle();
		String lines = csvLines(stocks);
		String data = String.join(Strings.LINE_SEPARATOR, title, lines);
		PutObjectResult result = putStockData(data);
		log.info("writeStocks result : {}", result);
	}

	@NotNull
	private String csvTitle() {
		return String.join(CSV_DELIMITER, "stockCode", "tickerSymbol", "companyName", "companyNameEng",
			"sector", "market");
	}

	@NotNull
	private String csvLines(List<Stock> stocks) {
		return stocks.stream()
			.map(Stock::toCsvLine)
			.collect(Collectors.joining(Strings.LINE_SEPARATOR));
	}

	private PutObjectResult putStockData(String data) {
		PutObjectRequest request;
		try (InputStream inputStream = new ByteArrayInputStream(data.getBytes(UTF_8))) {
			request = new PutObjectRequest(bucketName, stockPath, inputStream, createObjectMetadata());
		} catch (IOException e) {
			throw new IllegalStateException("Item data input/output error", e);
		}
		return amazonS3.putObject(request);
	}

	@NotNull
	private ObjectMetadata createObjectMetadata() {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType("text/csv");
		return metadata;
	}
}
