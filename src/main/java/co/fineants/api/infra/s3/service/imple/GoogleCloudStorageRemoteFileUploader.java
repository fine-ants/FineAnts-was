package co.fineants.api.infra.s3.service.imple;

import co.fineants.api.domain.member.domain.entity.ProfileImageFile;
import co.fineants.api.infra.s3.service.RemoteFileUploader;

// todo: 구글 클라우드 스토리지 업로더 구현
public class GoogleCloudStorageRemoteFileUploader implements RemoteFileUploader {
	@Override
	public void upload(String fileContent, String filePath) {

	}

	@Override
	public String uploadImageFile(ProfileImageFile profileImageFile, String filePath) {
		return null;
	}
}
