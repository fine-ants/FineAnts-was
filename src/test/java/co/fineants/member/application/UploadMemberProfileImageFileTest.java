package co.fineants.member.application;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.global.errors.exception.business.MemberProfileUploadException;

class UploadMemberProfileImageFileTest extends AbstractContainerBaseTest {

	@Autowired
	private UploadMemberProfileImageFile uploadMemberProfileImageFile;

	@DisplayName("파일이 없는 경우에는 비어있는 Optional을 반환한다")
	@ParameterizedTest
	@MethodSource(value = "co.fineants.TestDataProvider#invalidProfileFileSource")
	void upload_whenFileIsEmpty_thenReturnEmptyOptional(MultipartFile file, String ignored) {
		// given
		// when
		Optional<String> profileUrl = uploadMemberProfileImageFile.upload(file);
		// then
		assertThat(profileUrl).isEmpty();
	}

	@DisplayName("사용자는 프로필 이미지 사이즈를 초과하여 이미지를 업로드할 수 없다")
	@Test
	void upload_whenOverProfileImageFile_thenThrowException() {
		// given
		MultipartFile profileFile = TestDataFactory.createOverSizeMockProfileFile(); // 3MB
		// when
		Throwable throwable = catchThrowable(() -> uploadMemberProfileImageFile.upload(profileFile));
		// then
		assertThat(throwable)
			.isInstanceOf(MemberProfileUploadException.class);
	}
}
