package co.fineants.api.infra.s3.service.imple;

import java.nio.charset.StandardCharsets;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;

import co.fineants.api.domain.member.domain.entity.ProfileImageFile;
import co.fineants.api.infra.s3.service.RemoteFileUploader;

// todo: 구글 클라우드 스토리지 업로더 구현
public class GoogleCloudStorageRemoteFileUploader implements RemoteFileUploader {

	private final Storage storage;
	private final String bucketName;

	public GoogleCloudStorageRemoteFileUploader(Storage storage, String bucketName) {
		this.storage = storage;
		this.bucketName = bucketName;
	}

	@Override
	public void upload(String fileContent, String filePath) {
		BlobId blobId = BlobId.of(bucketName, filePath);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
		storage.create(blobInfo, fileContent.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public String uploadImageFile(ProfileImageFile profileImageFile, String filePath) {
		return null;
	}
}
