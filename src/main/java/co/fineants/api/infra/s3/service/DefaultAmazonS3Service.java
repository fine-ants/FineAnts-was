package co.fineants.api.infra.s3.service;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultAmazonS3Service implements AmazonS3Service {

	private final AmazonS3 amazonS3;
	@Value("${aws.s3.bucket}")
	private String bucketName;
	@Value("${aws.s3.profile-path}")
	private String profilePath;

	@Override
	public void deleteProfileImageFile(String url) {
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
