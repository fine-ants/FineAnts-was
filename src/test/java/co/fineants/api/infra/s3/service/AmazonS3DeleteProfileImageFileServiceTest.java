package co.fineants.api.infra.s3.service;

import java.net.MalformedURLException;
import java.net.URL;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;

class AmazonS3DeleteProfileImageFileServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private DeleteProfileImageFileService service;

	@Autowired
	private WriteProfileImageFileService writeService;

	@Autowired
	private RemoteFileFetcher fetcher;

	@Test
	void canCreated() {
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void delete() {
		// 이미지 파일 저장하기
		MultipartFile profileFile = TestDataFactory.createProfileFile();
		String url = writeService.upload(profileFile);
		String path = extractPathFromUrl(url);
		Assertions.assertThat(fetcher.read(path)).isPresent();
		// 이미지 파일 삭제
		service.delete(url);
		// 이미지 파일이 삭제되었는지 확인
		Assertions.assertThat(fetcher.read(path)).isEmpty();
	}

	private String extractPathFromUrl(String url) {
		try {
			URL u = new URL(url);
			String path = u.getPath(); // -> "/fineants2024/local/profile/46e5f63c-074a-467c-bccb-06ef9968290eprofile.jpeg"
			return path.substring(
				"/fineants2024/".length()); // -> "local/profile/46e5f63c-074a-467c-bccb-06ef9968290eprofile.jpeg"
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Invalid URL: " + url, e);
		}
	}
}
