package co.fineants.api.infra.s3.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.amazonaws.services.s3.AmazonS3;

import joptsimple.internal.Strings;

class AmazonBucketS3RemoteFileUploaderTest {

	private RemoteFileUploader fileUploader;
	private AmazonS3 amazonS3;

	@BeforeEach
	void setUp() {
		String bucketName = "fineants2024";
		amazonS3 = Mockito.mock(AmazonS3.class);
		fileUploader = new AmazonBucketS3RemoteFileUploader(bucketName, amazonS3);
	}

	@Test
	void canCreated() {
		Assertions.assertThat(fileUploader).isNotNull();
	}

	@Test
	void upload_whenFileContentIsEmpty_thenUploadEmptyFile() {
		String fileContent = Strings.EMPTY;
		String filePath = "local/dividend/dividends.csv";

		Assertions.assertThatCode(() -> fileUploader.upload(fileContent, filePath))
			.doesNotThrowAnyException();
		Mockito.verify(amazonS3, Mockito.times(1)).putObject(Mockito.any());
	}
}
