package co.fineants.api.infra.s3.service;

import static java.nio.charset.StandardCharsets.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jetbrains.annotations.NotNull;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;

public class AmazonS3WriteDividendService implements WriteDividendService {

	private final DividendCsvFormatter formatter;
	private final String bucketName;
	private final String dividendPath;
	private final AmazonS3 amazonS3;

	public AmazonS3WriteDividendService(DividendCsvFormatter formatter, String bucketName, String dividendPath,
		AmazonS3 amazonS3) {
		this.formatter = formatter;
		this.bucketName = bucketName;
		this.dividendPath = dividendPath;
		this.amazonS3 = amazonS3;
	}

	@Override
	public void writeDividend(StockDividend... dividends) {
		putObject(formatter.format(dividends));
	}

	private PutObjectResult putObject(String data) {
		PutObjectRequest request;
		try (InputStream inputStream = new ByteArrayInputStream(data.getBytes(UTF_8))) {
			request = new PutObjectRequest(bucketName, dividendPath, inputStream, createObjectMetadata());
		} catch (IOException e) {
			throw new IllegalStateException("Dividend data input/output error", e);
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
