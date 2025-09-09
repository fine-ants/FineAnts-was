package co.fineants.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;

@TestConfiguration
public class GoogleCloudStorageBucketInitializer {
	@Value("${gcp.storage.bucket}")
	private String bucketName;

	@Bean
	public ApplicationRunner bucketInitializer(Storage storage) {
		return args -> {
			if (storage.get(bucketName) == null) {
				storage.create(BucketInfo.of(bucketName));
			}
		};
	}
}
