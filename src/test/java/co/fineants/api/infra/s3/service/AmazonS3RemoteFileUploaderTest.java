package co.fineants.api.infra.s3.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import co.fineants.AbstractContainerBaseTest;

class AmazonS3RemoteFileUploaderTest extends AbstractContainerBaseTest {

	@Autowired
	@Qualifier("amazonS3RemoteFileUploader")
	private RemoteFileUploader fileUploader;

	@Autowired
	private RemoteFileFetcher fileFetcher;

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
		new FileContentComparator().compare(inputStream, "src/test/resources/gold_empty_dividends.csv");
	}

	@Test
	void updateImageFile() {
		MultipartFile profileFile = createProfileFile();
		String profilePath = "local/profile/";

		String path = fileUploader.uploadImageFile(profileFile, profilePath);

		String expectedPath = "https://fineants2024.s3.ap-northeast-2.amazonaws.com/local/profile/001d55f2-ce0b-49b9-b55c-4130d305a3f4profile.jpeg";
		Assertions.assertThat(path).isEqualTo(expectedPath);
	}
}
