package co.fineants.api.infra.s3.service.imple;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;

import co.fineants.member.domain.ProfileImageFile;
import co.fineants.api.infra.s3.service.RemoteFileUploader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
		Blob blob = storage.create(blobInfo, fileContent.getBytes(StandardCharsets.UTF_8));
		log.info("File uploaded to bucket {} with path {}. Blob info: {}", bucketName, filePath, blob);
	}

	@Override
	public String uploadImageFile(ProfileImageFile profileImageFile, String filePath) {
		Blob blob;
		try (FileInputStream fileInputStream = new FileInputStream(profileImageFile.getFile())) {
			BlobId blobId = BlobId.of(bucketName, filePath);
			BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
				.setContentType(profileImageFile.getContentType())
				.setAcl(Collections.singletonList(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)))
				.build();
			blob = storage.create(blobInfo, fileInputStream.readAllBytes());
		} catch (Exception e) {
			throw new IllegalStateException("Failed to upload file to GCS", e);
		}

		return String.format("%s%s/%s", storage.getOptions().getHost(), blob.getBucket(), blob.getName());
	}
}
