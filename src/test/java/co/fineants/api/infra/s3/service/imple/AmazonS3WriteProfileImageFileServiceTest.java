package co.fineants.api.infra.s3.service.imple;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.holding.domain.factory.UuidGenerator;
import co.fineants.member.domain.ProfileImageFile;
import co.fineants.api.global.errors.exception.business.ImageEmptyInvalidInputException;
import co.fineants.api.infra.s3.service.RemoteFileUploader;
import co.fineants.api.infra.s3.service.WriteProfileImageFileService;

class AmazonS3WriteProfileImageFileServiceTest {

	private WriteProfileImageFileService service;

	@BeforeEach
	void setUp() {
		String host = "http://localhost:54566/";
		String bucketName = "fineants2024/";
		String filePath = "local/profile/";
		String uuid = "001d55f2-ce0b-49b9-b55c-4130d305a3f4";
		String key = host + bucketName + filePath + uuid + "profile.jpeg";
		RemoteFileUploader uploader = Mockito.mock(RemoteFileUploader.class);
		given(uploader.uploadImageFile(any(ProfileImageFile.class), anyString()))
			.willReturn(key);
		UuidGenerator uuidGenerator = Mockito.mock(UuidGenerator.class);
		given(uuidGenerator.generate())
			.willReturn(uuid);
		service = new AmazonS3WriteProfileImageFileService(uploader, filePath, uuidGenerator);
	}

	@Test
	void canCreated() {
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void upload() {
		MultipartFile multipartFile = TestDataFactory.createProfileFile();
		String key = service.upload(multipartFile);

		String expectedKey = "http://localhost:54566/fineants2024/local/profile/001d55f2-ce0b-49b9-b55c-4130d305a3f4profile.jpeg";
		Assertions.assertThat(key).isEqualTo(expectedKey);
	}

	@Test
	void upload_whenFileIsNull() {
		MultipartFile multipartFile = null;

		Throwable throwable = Assertions.catchThrowable(() -> service.upload(multipartFile));

		Assertions.assertThat(throwable)
			.isInstanceOf(ImageEmptyInvalidInputException.class);
	}
}
