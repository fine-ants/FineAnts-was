package co.fineants.api.infra.s3.service;

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
		String key = writeService.upload(profileFile);
		// 이미지 파일 삭제
		service.delete(key);
		// 이미지 파일이 삭제되었는지 확인
		Assertions.assertThat(fetcher.read(key)).isEmpty();
	}

}
