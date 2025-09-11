package co.fineants.api.infra.s3.service.imple;

import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.infra.s3.service.RemoteFileFetcher;
import co.fineants.config.GoogleCloudStorageBucketInitializer;
import co.fineants.config.GoogleCloudStorageTestConfig;

@ActiveProfiles(value = {"test", "gcp"}, inheritProfiles = false)
@ContextConfiguration(classes = {GoogleCloudStorageTestConfig.class, GoogleCloudStorageBucketInitializer.class})
class GoogleCloudStorageRemoteFileFetcherTest extends AbstractContainerBaseTest {

	@Autowired
	private RemoteFileFetcher remoteFileFetcher;

	public static Stream<Arguments> invalidPathSource() {
		return Stream.of(
			Arguments.of((Object)null),
			Arguments.of(""),
			Arguments.of("   "),
			Arguments.of("non/existent/path/file.txt")
		);
	}

	@Test
	void canCreated() {
		Assertions.assertThat(remoteFileFetcher).isNotNull();
	}

	@ParameterizedTest
	@MethodSource("invalidPathSource")
	void read_whenInvalidPath_thenReturnEmptyOptional(String path) {
		Optional<InputStream> inputStream = remoteFileFetcher.read(path);

		Assertions.assertThat(inputStream).isEmpty();
	}
}
