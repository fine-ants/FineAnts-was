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

import co.fineants.api.domain.dividend.domain.parser.StockDividendParser;
import co.fineants.api.infra.s3.service.DeleteDividendService;
import co.fineants.api.infra.s3.service.DeleteProfileImageFileService;
import co.fineants.api.infra.s3.service.FetchDividendService;
import co.fineants.api.infra.s3.service.RemoteFileFetcher;
import co.fineants.api.infra.s3.service.imple.AmazonS3DeleteDividendService;
import co.fineants.api.infra.s3.service.imple.AmazonS3DeleteProfileImageFileService;
import co.fineants.api.infra.s3.service.imple.AmazonS3FetchDividendService;
import lombok.extern.slf4j.Slf4j;

@Profile(value = {"local", "release", "production"})
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
		@Value("${aws.s3.dividend-csv-path}") String dividendPath, StockDividendParser stockDividendParser) {
		return new AmazonS3FetchDividendService(fileFetcher, dividendPath, stockDividendParser);
	}
}
