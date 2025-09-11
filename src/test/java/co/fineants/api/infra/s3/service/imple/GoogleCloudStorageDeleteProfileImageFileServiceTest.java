package co.fineants.api.infra.s3.service.imple;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.multipart.MultipartFile;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.infra.s3.service.DeleteProfileImageFileService;
import co.fineants.api.infra.s3.service.RemoteFileFetcher;
import co.fineants.api.infra.s3.service.WriteProfileImageFileService;
import co.fineants.config.GoogleCloudStorageBucketInitializer;
import co.fineants.config.GoogleCloudStorageTestConfig;

@ActiveProfiles(value = {"test", "gcp"}, inheritProfiles = false)
@ContextConfiguration(classes = {GoogleCloudStorageTestConfig.class, GoogleCloudStorageBucketInitializer.class})
class GoogleCloudStorageDeleteProfileImageFileServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private DeleteProfileImageFileService service;

	@Autowired
	private WriteProfileImageFileService writeProfileImageFileService;

	@Autowired
	private RemoteFileFetcher remoteFileFetcher;

	@Value("${gcp.storage.profile-path}")
	private String profilePath;

	private String url;

	@BeforeEach
	void setUp() {
		MultipartFile profileFile = TestDataFactory.createProfileFile();
		url = writeProfileImageFileService.upload(profileFile);
	}

	@Test
	void canCreated() {
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void delete() {
		String fileName = url.substring(url.lastIndexOf("/") + 1);
		String path = profilePath + fileName;
		Assertions.assertThat(remoteFileFetcher.read(path)).isPresent();

		service.delete(url);

		Assertions.assertThat(remoteFileFetcher.read(path)).isEmpty();
	}
}
