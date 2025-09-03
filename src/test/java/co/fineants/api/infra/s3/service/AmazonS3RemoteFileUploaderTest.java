package co.fineants.api.infra.s3.service;

import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import co.fineants.AbstractContainerBaseTest;

class AmazonS3RemoteFileUploaderTest extends AbstractContainerBaseTest {

	@Autowired
	@Qualifier("amazonS3RemoteFileUploader")
	private RemoteFileUploader fileUploader;

	@Autowired
	private RemoteFileFetcher fileFetcher;

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
}
