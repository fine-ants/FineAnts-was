package co.fineants.api.infra.s3.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

import co.fineants.api.global.errors.errorcode.MemberErrorCode;
import co.fineants.api.global.errors.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultAmazonS3Service implements AmazonS3Service {
	private static final int MAX_FILE_SIZE = 2 * 1024 * 1024;

	private final AmazonS3 amazonS3;
	@Value("${aws.s3.bucket}")
	private String bucketName;
	@Value("${aws.s3.profile-path}")
	private String profilePath;

	@Transactional
	@Override
	public String upload(MultipartFile multipartFile) throws BadRequestException {
		File file = convertMultiPartFileToFile(multipartFile).orElseThrow(
			() -> new BadRequestException(MemberErrorCode.PROFILE_IMAGE_UPLOAD_FAIL));
		// random file name
		String key = profilePath + UUID.randomUUID() + file.getName();
		// put S3
		amazonS3.putObject(new PutObjectRequest(bucketName, key, file).withCannedAcl(
			CannedAccessControlList.PublicRead));
		// get S3
		String path = amazonS3.getUrl(bucketName, key).toString();
		// delete object
		try {
			cleanup(file.toPath());
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return path;
	}

	private void cleanup(Path path) throws IOException {
		Files.delete(path);
	}

	private Optional<File> convertMultiPartFileToFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return Optional.empty();
		}
		if (file.getSize() > MAX_FILE_SIZE) {
			throw new BadRequestException(MemberErrorCode.IMAGE_SIZE_EXCEEDED);
		}
		String filename = file.getOriginalFilename();
		if (filename == null) {
			throw new BadRequestException(MemberErrorCode.PROFILE_IMAGE_UPLOAD_FAIL);
		}
		File convertedFile = new File(filename);
		try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
			fos.write(file.getBytes());
		} catch (IOException e) {
			throw new BadRequestException(MemberErrorCode.PROFILE_IMAGE_UPLOAD_FAIL);
		}
		return Optional.of(convertedFile);
	}

	@Override
	public void deleteFile(String url) {
		try {
			String fileName = extractFileName(url);
			amazonS3.deleteObject(bucketName, fileName);
		} catch (AmazonServiceException e) {
			log.error(e.getMessage());
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
