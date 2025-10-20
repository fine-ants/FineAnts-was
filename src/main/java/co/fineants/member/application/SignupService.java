package co.fineants.member.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.validator.domain.member.SignUpValidator;
import co.fineants.api.infra.s3.service.DeleteProfileImageFileService;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignupService {

	private final SignUpValidator signUpValidator;
	private final MemberRepository memberRepository;
	private final DeleteProfileImageFileService deleteProfileImageFileService;

	@Transactional
	public void signup(Member member) {
		// 회원 정보 검증
		signUpValidator.validate(member);
		// 회원 저장
		memberRepository.save(member);
	}

	public void deleteProfileImageFile(String profileUrl) {
		deleteProfileImageFileService.delete(profileUrl);
	}
}
