package co.fineants.api.infra.s3.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.amazonaws.services.s3.AmazonS3;

import joptsimple.internal.Strings;

class AmazonBucketS3FileUploaderTest {

	@Test
	void canCreated() {
		String bucketName = "fineants2024";
		AmazonS3 amazonS3 = Mockito.mock(AmazonS3.class);
		FileUploader fileUploader = new AmazonBucketS3FileUploader(bucketName, amazonS3);

		Assertions.assertThat(fileUploader).isNotNull();
	}

	@Test
	void upload_whenFileContentIsEmpty_thenUploadEmptyFile() {
		String bucketName = "fineants2024";
		AmazonS3 amazonS3 = Mockito.mock(AmazonS3.class);
		FileUploader fileUploader = new AmazonBucketS3FileUploader(bucketName, amazonS3);
		String fileContent = Strings.EMPTY;
		String filePath = "local/dividend/dividends.csv";

		Assertions.assertThatCode(() -> fileUploader.upload(fileContent, filePath))
			.doesNotThrowAnyException();
	}
}
