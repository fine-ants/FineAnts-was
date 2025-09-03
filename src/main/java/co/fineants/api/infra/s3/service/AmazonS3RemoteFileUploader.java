package co.fineants.api.infra.s3.service;

import static java.nio.charset.StandardCharsets.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import co.fineants.api.domain.holding.domain.factory.UuidGenerator;
import co.fineants.api.global.errors.exception.business.ImageEmptyInvalidInputException;
import co.fineants.api.global.errors.exception.business.ImageNameEmptyInvalidInputException;
import co.fineants.api.global.errors.exception.business.ImageSizeExceededInvalidInputException;
import co.fineants.api.global.errors.exception.business.ImageWriteInvalidInputException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AmazonS3RemoteFileUploader implements RemoteFileUploader {

	private static final int MAX_FILE_SIZE = 2 * 1024 * 1024;

	private final String bucketName;

	private final AmazonS3 amazonS3;
	private UuidGenerator uuidGenerator;

	public AmazonS3RemoteFileUploader(
		@Value("${aws.s3.bucket}") String bucketName,
		AmazonS3 amazonS3,
		UuidGenerator uuidGenerator) {
		this.bucketName = bucketName;
		this.amazonS3 = amazonS3;
		this.uuidGenerator = uuidGenerator;
	}

	@Override
	public void upload(String fileContent, String filePath) {
		PutObjectRequest request;
		try (InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes(UTF_8))) {
			request = new PutObjectRequest(bucketName, filePath, inputStream, createObjectMetadata());
		} catch (IOException e) {
			throw new IllegalStateException("Dividend data input/output error", e);
		}
		amazonS3.putObject(request);
	}

	@NotNull
	private ObjectMetadata createObjectMetadata() {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType("text/csv");
		return metadata;
	}

	@Override
	public String uploadImageFile(MultipartFile multipartFile, String profilePath) {
		File file = convertMultiPartFileToFile(multipartFile);
		String key = profilePath + uuidGenerator.generate() + file.getName();

		amazonS3.putObject(new PutObjectRequest(bucketName, key, file)
			.withCannedAcl(CannedAccessControlList.PublicRead));
		// delete object
		try {
			Files.delete(file.toPath());
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return key;
	}

	private File convertMultiPartFileToFile(MultipartFile file) throws
		ImageEmptyInvalidInputException,
		ImageSizeExceededInvalidInputException,
		ImageNameEmptyInvalidInputException,
		ImageWriteInvalidInputException {
		if (file == null || file.isEmpty()) {
			throw new ImageEmptyInvalidInputException();
		}
		if (file.getSize() > MAX_FILE_SIZE) {
			throw new ImageSizeExceededInvalidInputException(file);
		}
		String filename = file.getOriginalFilename();
		if (filename == null) {
			throw new ImageNameEmptyInvalidInputException(filename);
		}
		File convertedFile = new File(filename);
		try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
			fos.write(file.getBytes());
		} catch (IOException e) {
			throw new ImageWriteInvalidInputException(convertedFile);
		}
		return convertedFile;
	}
}
