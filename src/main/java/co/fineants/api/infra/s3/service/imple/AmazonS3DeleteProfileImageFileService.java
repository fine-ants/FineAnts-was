package co.fineants.api.infra.s3.service.imple;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;

import co.fineants.api.infra.s3.service.DeleteProfileImageFileService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AmazonS3DeleteProfileImageFileService implements DeleteProfileImageFileService {

	private final String bucketName;
	private final String profilePath;
	private final AmazonS3 amazonS3;

	public AmazonS3DeleteProfileImageFileService(
		@Value("${aws.s3.bucket}") String bucketName,
		@Value("${aws.s3.profile-path}") String profilePath,
		AmazonS3 amazonS3) {
		this.bucketName = bucketName;
		this.profilePath = profilePath;
		this.amazonS3 = amazonS3;
	}

	@Override
	public void delete(String url) {
		if (Strings.isBlank(url)) {
			return;
		}
		String fileName = extractFileName(url);
		String key = profilePath + fileName;
		try {
			amazonS3.deleteObject(bucketName, key);
		} catch (AmazonServiceException e) {
			log.warn("Failed to delete file from S3: " + e.getMessage(), e);
		}
	}

	/**
	 * 파일 URL에서 이름만 추출하여 반환한다
	 * @param url 파일 URL
	 * - 예: <a href="https://fineants.s3.ap-northeast-2.amazonaws.com/9d07ee41-4404-414b-9ee7-12616aa6bedcprofile.jpeg">...</a>
	 * @return 파일 이름
	 */
	private String extractFileName(String url) {
		int lastSlashIndex = url.lastIndexOf('/');
		return url.substring(lastSlashIndex + 1);
	}
}
