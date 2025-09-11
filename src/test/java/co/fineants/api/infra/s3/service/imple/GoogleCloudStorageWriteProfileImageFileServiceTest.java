package co.fineants.api.infra.s3.service.imple;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.Storage;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.holding.domain.factory.UuidGenerator;
import co.fineants.api.infra.s3.service.RemoteFileUploader;
import co.fineants.api.infra.s3.service.WriteProfileImageFileService;
import co.fineants.config.GoogleCloudStorageBucketInitializer;
import co.fineants.config.GoogleCloudStorageTestConfig;

@ActiveProfiles(value = {"test", "gcp"}, inheritProfiles = false)
@ContextConfiguration(classes = {GoogleCloudStorageTestConfig.class, GoogleCloudStorageBucketInitializer.class})
class GoogleCloudStorageWriteProfileImageFileServiceTest extends AbstractContainerBaseTest {

	private WriteProfileImageFileService service;

	@Autowired
	private RemoteFileUploader uploader;

	@Autowired
	private Storage storage;

	@Value("${gcp.storage.profile-path}")
	private String profilePath;

	@Value("${gcp.storage.bucket}")
	private String bucketName;

	private String uuid;

	@BeforeEach
	void setUp() {
		UuidGenerator uuidGenerator = Mockito.mock(UuidGenerator.class);
		uuid = "faefd951-ee7b-455d-a058-2e11817a8e6a";
		BDDMockito.given(uuidGenerator.generate())
			.willReturn(uuid);
		service = new GoogleCloudStorageWriteProfileImageFileService(uploader, profilePath, uuidGenerator);
	}

	@Test
	void canCreated() {
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void upload() {
		MultipartFile profileFile = TestDataFactory.createProfileFile();

		String path = service.upload(profileFile);

		String expected = storage.getOptions().getHost() + "/" + bucketName + "/" + profilePath + uuid
			+ profileFile.getOriginalFilename();
		Assertions.assertThat(path).isEqualTo(expected);
	}
}
