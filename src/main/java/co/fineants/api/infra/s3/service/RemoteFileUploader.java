package co.fineants.api.infra.s3.service;

import co.fineants.api.infra.s3.service.imple.ProfileImageFile;

public interface RemoteFileUploader {
	void upload(String fileContent, String filePath);

	/**
	 * 원격 저장소에 프로필 이미지 파일을 filePath에 업로드하고, 업로드된 파일의 URL을 반환한다.
	 * <p>
	 * 프로필 이미지 파일의 이름이 profile.jpeg, filePath가 "local/profile/"이라면
	 * 업로드된 파일의 URL은 <a href="http://127.0.0.1:51796/fineants2024/local/profile/001d55f2-ce0b-49b9-b55c-4130d305a3f4profile.jpeg">...</a> 와 같은 형식이다
	 * </p>
	 * @param profileImageFile 프로필 이미지 파일 객체
	 * @param filePath 파일이 저장될 경로
	 * @return 업로드된 파일의 URL
	 */
	String uploadImageFile(ProfileImageFile profileImageFile, String filePath);
}
