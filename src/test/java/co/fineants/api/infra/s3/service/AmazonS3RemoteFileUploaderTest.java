package co.fineants.api.infra.s3.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.holding.domain.factory.UuidGenerator;

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

	private static MultipartFile createProfileFile() {
		ClassPathResource classPathResource = new ClassPathResource("profile.jpeg");
		try {
			Path path = Paths.get(classPathResource.getURI());
			byte[] profile = Files.readAllBytes(path);
			return new MockMultipartFile("profileImageFile", "profile.jpeg", "image/jpeg",
				profile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@BeforeEach
	void setUp() {
		UuidGenerator uuidGenerator = Mockito.mock(UuidGenerator.class);
		BDDMockito.given(uuidGenerator.generate())
			.willReturn("001d55f2-ce0b-49b9-b55c-4130d305a3f4");
		fileUploader = new AmazonS3RemoteFileUploader(bucketName, amazonS3, uuidGenerator);
	}

	@Test
	void canCreated() {
		Assertions.assertThat(fileUploader).isNotNull();
	}

	@Test
	void upload_whenFileContentIsEmpty_thenUploadEmptyFile() {
		String fileContent = "id,dividend,recordDate,paymentDate,stockCode";
		String filePath = "local/dividend/dividends.csv";

		fileUploader.upload(fileContent, filePath);

		InputStream inputStream = fileFetcher.read(filePath);
		Assertions.assertThat(inputStream).isNotNull();
		new FileContentComparator().compare(inputStream, "src/test/resources/gold_empty_dividends.csv");
	}

	@Test
	void updateImageFile() {
		MultipartFile profileFile = createProfileFile();
		ProfileImageFile profileImageFile = new ProfileImageFile(profileFile);

		String path = fileUploader.uploadImageFile(profileImageFile, profilePath);

		String expectedPath = "local/profile/001d55f2-ce0b-49b9-b55c-4130d305a3f4profile.jpeg";
		Assertions.assertThat(path).isEqualTo(expectedPath);
	}
}
