package co.fineants.api.domain.member.domain.entity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.web.multipart.MultipartFile;

import co.fineants.api.global.errors.exception.business.ImageEmptyInvalidInputException;
import co.fineants.api.global.errors.exception.business.ImageNameEmptyInvalidInputException;
import co.fineants.api.global.errors.exception.business.ImageSizeExceededInvalidInputException;
import co.fineants.api.global.errors.exception.business.ImageWriteInvalidInputException;
import lombok.Getter;

@Getter
public class ProfileImageFile {

	private static final int MAX_FILE_SIZE = 2 * 1024 * 1024;

	private final File file;

	public ProfileImageFile(MultipartFile multipartFile) {
		this.file = convertMultiPartFileToFile(multipartFile);
	}

	public void deleteFile() {
		try {
			Files.delete(file.toPath());
		} catch (IOException e) {
			throw new IllegalStateException("Failed to delete temporary file", e);
		}
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

	public String getFileName() {
		return file.getName();
	}

	public String getContentType() {
		try {
			return Files.probeContentType(file.toPath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
