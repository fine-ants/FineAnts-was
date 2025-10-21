package co.fineants.api.infra.s3.service.imple;

import java.io.InputStream;

import org.apache.logging.log4j.util.Strings;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.holding.domain.factory.UuidGenerator;
import co.fineants.member.domain.ProfileImageFile;
import co.fineants.api.infra.s3.service.RemoteFileFetcher;
import co.fineants.api.infra.s3.service.RemoteFileUploader;

class AmazonS3RemoteFileUploaderTest extends AbstractContainerBaseTest {

	private RemoteFileUploader fileUploader;

	@Value("${aws.s3.bucket}")
	private String bucketName;

	@Autowired
	private AmazonS3 amazonS3;

	@Autowired
	private RemoteFileFetcher fileFetcher;

	@Value("${aws.s3.profile-path}")
	private String profilePath;
	private UuidGenerator uuidGenerator;

	@BeforeEach
	void setUp() {
		uuidGenerator = Mockito.mock(UuidGenerator.class);
		BDDMockito.given(uuidGenerator.generate())
			.willReturn("001d55f2-ce0b-49b9-b55c-4130d305a3f4");
		fileUploader = new AmazonS3RemoteFileUploader(bucketName, amazonS3);
	}

	@Test
	void canCreated() {
		Assertions.assertThat(fileUploader).isNotNull();
	}

	@Test
	void upload_whenFileContentIsEmpty_thenUploadEmptyFile() {
		String fileContent = "tickerSymbol,dividend,recordDate,paymentDate,isDeleted";
		String filePath = "local/dividend/dividends.csv";

		fileUploader.upload(fileContent, filePath);

		InputStream inputStream = fileFetcher.read(filePath).orElseThrow();
		Assertions.assertThat(inputStream).isNotNull();
		FileContentComparator.compare(inputStream, "src/test/resources/gold_empty_dividends.csv");
	}

	@Test
	void upload_whenFileContentIsInvalid_thenNotUpload() {
		String fileContent = null;
		String filePath = "local/dividend/dividends.csv";

		Throwable throwable = Assertions.catchThrowable(() -> fileUploader.upload(fileContent, filePath));

		Assertions.assertThat(throwable)
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void upload_whenFilePathIsInvalid_thenNotUpload() {
		String fileContent = "id,dividend,recordDate,paymentDate,stockCode";

		Assertions.assertThatThrownBy(() -> fileUploader.upload(fileContent, null))
			.isInstanceOf(IllegalArgumentException.class);
		Assertions.assertThatThrownBy(() -> fileUploader.upload(fileContent, Strings.EMPTY))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void updateImageFile() {
		MultipartFile profileFile = TestDataFactory.createProfileFile();
		ProfileImageFile profileImageFile = new ProfileImageFile(profileFile);
		String key = profilePath + uuidGenerator.generate() + profileImageFile.getFileName();

		String saveUrl = fileUploader.uploadImageFile(profileImageFile, key);

		String host = LOCAL_STACK_CONTAINER.getEndpoint().toString();
		String expectedUrl = host + "/fineants2024/local/profile/001d55f2-ce0b-49b9-b55c-4130d305a3f4profile.jpeg";
		Assertions.assertThat(saveUrl).isEqualTo(expectedUrl);

		profileImageFile.deleteFile();
	}
}
