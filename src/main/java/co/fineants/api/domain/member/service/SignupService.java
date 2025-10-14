package co.fineants.api.domain.member.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import co.fineants.api.domain.validator.domain.member.SignUpValidator;
import co.fineants.api.global.errors.exception.business.ImageEmptyInvalidInputException;
import co.fineants.api.global.errors.exception.business.InvalidInputException;
import co.fineants.api.global.errors.exception.business.MemberProfileUploadException;
import co.fineants.api.infra.s3.service.DeleteProfileImageFileService;
import co.fineants.api.infra.s3.service.WriteProfileImageFileService;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignupService {

	private final SignUpValidator signUpValidator;
	private final MemberRepository memberRepository;
	private final WriteProfileImageFileService writeProfileImageFileService;
	private final DeleteProfileImageFileService deleteProfileImageFileService;

	@Transactional
	public void signup(Member member) {
		// 회원 정보 검증
		signUpValidator.validate(member);
		// 회원 저장
		memberRepository.save(member);
	}

	public Optional<String> upload(MultipartFile file) throws MemberProfileUploadException {
		try {
			return Optional.of(writeProfileImageFileService.upload(file));
		} catch (ImageEmptyInvalidInputException e) {
			return Optional.empty();
		} catch (InvalidInputException e) {
			throw new MemberProfileUploadException(file, e);
		}
	}

	public void deleteProfileImageFile(String profileUrl) {
		deleteProfileImageFileService.delete(profileUrl);
	}
}
