package co.fineants.member.application;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import co.fineants.api.global.errors.exception.business.ImageEmptyInvalidInputException;
import co.fineants.api.global.errors.exception.business.InvalidInputException;
import co.fineants.api.global.errors.exception.business.MemberProfileUploadException;
import co.fineants.api.infra.s3.service.WriteProfileImageFileService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadMemberProfileImageFile {

	private final WriteProfileImageFileService service;

	public Optional<String> upload(MultipartFile file) throws MemberProfileUploadException {
		try {
			return Optional.of(service.upload(file));
		} catch (ImageEmptyInvalidInputException e) {
			return Optional.empty();
		} catch (InvalidInputException e) {
			throw new MemberProfileUploadException(file, e);
		}
	}
}
