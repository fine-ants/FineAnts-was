package co.fineants.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import io.aiven.testcontainers.fakegcsserver.FakeGcsServerContainer;

@TestConfiguration
public class GoogleCloudStorageTestConfig {

	@Bean(initMethod = "start", destroyMethod = "stop")
	public FakeGcsServerContainer gcsContainer() {
		return new FakeGcsServerContainer()
			.withExposedPorts(4443)
			.withReuse(true);
	}

	@Bean
	public Storage storage(FakeGcsServerContainer container) {
		String endpoint = String.format("http://%s:%d/", container.getHost(), container.getFirstMappedPort());
		return StorageOptions.newBuilder()
			.setHost(endpoint)
			.setProjectId("test-project")
			.build()
			.getService();
	}
}
