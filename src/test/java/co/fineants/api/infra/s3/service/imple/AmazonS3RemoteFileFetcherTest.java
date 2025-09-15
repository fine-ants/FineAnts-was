package co.fineants.api.infra.s3.service.imple;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import co.fineants.api.infra.s3.service.RemoteFileFetcher;

class AmazonS3RemoteFileFetcherTest {

	@Test
	void canCreated() {
		String bucketName = "fineants2024";
		AmazonS3 amazonS3 = Mockito.mock(AmazonS3.class);
		RemoteFileFetcher fileReader = new AmazonS3RemoteFileFetcher(bucketName, amazonS3);
		Assertions.assertThat(fileReader).isNotNull();
	}

	@Test
	void read() {
		String bucketName = "fineants2024";
		String filePath = "local/dividend/dividends.csv";
		AmazonS3 amazonS3 = Mockito.mock(AmazonS3.class);
		S3Object s3Object = Mockito.mock(S3Object.class);
		BDDMockito.given(amazonS3.getObject(bucketName, filePath))
			.willReturn(s3Object);
		BDDMockito.given(s3Object.getObjectContent())
			.willReturn(new S3ObjectInputStream(getMockInputStream(), null));
		RemoteFileFetcher fileReader = new AmazonS3RemoteFileFetcher(bucketName, amazonS3);

		InputStream inputStream = fileReader.read(filePath).orElseThrow();

		FileContentComparator.compare(inputStream, "src/test/resources/gold_dividends.csv");
	}

	private InputStream getMockInputStream() {
		try {
			return new FileInputStream("src/test/resources/gold_dividends.csv");
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
