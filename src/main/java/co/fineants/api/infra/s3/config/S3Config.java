package co.fineants.api.infra.s3.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import co.fineants.api.domain.dividend.domain.parser.StockDividendCsvParser;
import co.fineants.api.domain.holding.domain.factory.UuidGenerator;
import co.fineants.stock.application.StockCsvParser;
import co.fineants.api.global.common.csv.CsvFormatter;
import co.fineants.api.infra.s3.service.DeleteDividendService;
import co.fineants.api.infra.s3.service.DeleteProfileImageFileService;
import co.fineants.api.infra.s3.service.DeleteStockService;
import co.fineants.api.infra.s3.service.FetchDividendService;
import co.fineants.api.infra.s3.service.FetchStockService;
import co.fineants.api.infra.s3.service.RemoteFileFetcher;
import co.fineants.api.infra.s3.service.RemoteFileUploader;
import co.fineants.api.infra.s3.service.WriteDividendService;
import co.fineants.api.infra.s3.service.WriteProfileImageFileService;
import co.fineants.api.infra.s3.service.WriteStockService;
import co.fineants.api.infra.s3.service.imple.AmazonS3DeleteDividendService;
import co.fineants.api.infra.s3.service.imple.AmazonS3DeleteProfileImageFileService;
import co.fineants.api.infra.s3.service.imple.AmazonS3DeleteStockService;
import co.fineants.api.infra.s3.service.imple.AmazonS3FetchDividendService;
import co.fineants.api.infra.s3.service.imple.AmazonS3FetchStockService;
import co.fineants.api.infra.s3.service.imple.AmazonS3RemoteFileFetcher;
import co.fineants.api.infra.s3.service.imple.AmazonS3RemoteFileUploader;
import co.fineants.api.infra.s3.service.imple.AmazonS3WriteDividendService;
import co.fineants.api.infra.s3.service.imple.AmazonS3WriteProfileImageFileService;
import co.fineants.api.infra.s3.service.imple.AmazonS3WriteStockService;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockDividend;
import lombok.extern.slf4j.Slf4j;

@Profile(value = {"aws"})
@Configuration
@Slf4j
public class S3Config {
	@Value("${aws.access-key}")
	private String accessKey;
	@Value("${aws.secret-key}")
	private String accessSecret;
	@Value("${aws.region.static}")
	private String region;
	@Value("${aws.s3.bucket}")
	private String bucket;

	@Bean
	public AmazonS3 amazonS3() {
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, accessSecret);
		return AmazonS3ClientBuilder.standard()
			.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(region).build();
	}

	@Bean
	public DeleteDividendService deleteDividendService(AmazonS3 amazonS3,
		@Value("${aws.s3.dividend-csv-path}") String dividendPath) {
		return new AmazonS3DeleteDividendService(bucket, dividendPath, amazonS3);
	}

	@Bean
	public DeleteProfileImageFileService deleteProfileImageFileService(AmazonS3 amazonS3,
		@Value("${aws.s3.profile-path}") String profilePath) {
		return new AmazonS3DeleteProfileImageFileService(bucket, profilePath, amazonS3);
	}

	@Bean
	public FetchDividendService fetchDividendService(RemoteFileFetcher fileFetcher,
		@Value("${aws.s3.dividend-csv-path}") String dividendPath,
		StockDividendCsvParser stockDividendCsvParser) {
		return new AmazonS3FetchDividendService(fileFetcher, dividendPath, stockDividendCsvParser);
	}

	@Bean
	public FetchStockService fetchStockService(RemoteFileFetcher fileFetcher,
		@Value("${aws.s3.stock-path}") String filePath, StockCsvParser stockCsvParser) {
		return new AmazonS3FetchStockService(fileFetcher, filePath, stockCsvParser);
	}

	@Bean
	public RemoteFileFetcher remoteFileFetcher(AmazonS3 amazonS3) {
		return new AmazonS3RemoteFileFetcher(bucket, amazonS3);
	}

	@Bean
	public RemoteFileUploader remoteFileUploader(AmazonS3 amazonS3) {
		return new AmazonS3RemoteFileUploader(bucket, amazonS3);
	}

	@Bean
	public WriteDividendService writeDividendService(
		CsvFormatter<StockDividend> formatter,
		RemoteFileUploader fileUploader,
		@Value("${aws.s3.dividend-csv-path}") String dividendPath) {
		return new AmazonS3WriteDividendService(formatter, fileUploader, dividendPath);
	}

	@Bean
	public WriteProfileImageFileService writeProfileImageFileService(RemoteFileUploader fileUploader,
		@Value("${aws.s3.profile-path}") String profilePath,
		UuidGenerator uuidGenerator) {
		return new AmazonS3WriteProfileImageFileService(fileUploader, profilePath, uuidGenerator);
	}

	@Bean
	public WriteStockService writeStockService(
		RemoteFileUploader fileUploader,
		@Value("${aws.s3.stock-path}") String filePath,
		CsvFormatter<Stock> formatter) {
		return new AmazonS3WriteStockService(fileUploader, filePath, formatter);
	}

	@Bean
	public DeleteStockService deleteStockService(AmazonS3 amazonS3,
		@Value("${aws.s3.stock-path}") String filePath) {
		return new AmazonS3DeleteStockService(amazonS3, bucket, filePath);
	}
}
