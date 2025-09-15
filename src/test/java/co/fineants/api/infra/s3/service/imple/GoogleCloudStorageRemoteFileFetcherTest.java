package co.fineants.api.infra.s3.service.imple;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.springframework.web.multipart.MultipartFile;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.infra.s3.service.RemoteFileFetcher;
import co.fineants.api.infra.s3.service.WriteProfileImageFileService;
import co.fineants.config.GoogleCloudStorageBucketInitializer;
import co.fineants.config.GoogleCloudStorageTestConfig;

@ActiveProfiles(value = {"test", "gcp"}, inheritProfiles = false)
@ContextConfiguration(classes = {GoogleCloudStorageTestConfig.class, GoogleCloudStorageBucketInitializer.class})
class GoogleCloudStorageRemoteFileFetcherTest extends AbstractContainerBaseTest {

	@Autowired
	private RemoteFileFetcher remoteFileFetcher;

	@Autowired
	private WriteProfileImageFileService writeProfileImageFileService;

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

	@Test
	void read() throws URISyntaxException, IOException {
		MultipartFile profileFile = TestDataFactory.createProfileFile();
		String url = writeProfileImageFileService.upload(profileFile);
		String path = parseProfilePath(url);

		Optional<InputStream> inputStream = remoteFileFetcher.read(path);

		Assertions.assertThat(inputStream).isPresent();
		Assertions.assertThat(inputStream.get().readAllBytes()).isEqualTo(profileFile.getBytes());
	}

	private String parseProfilePath(String url) throws URISyntaxException {
		URI uri = new URI(url);
		String fullPath = uri.getPath();
		return fullPath.substring(fullPath.indexOf("/", 1) + 1);
	}
}
