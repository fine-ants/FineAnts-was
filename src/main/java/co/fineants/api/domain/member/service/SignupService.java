package co.fineants.api.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.rule.SignUpValidator;
import co.fineants.api.domain.member.repository.MemberRepository;

@Service
public class SignupService {

	private final SignUpValidator signUpValidator;
	private final MemberRepository memberRepository;
	private final MemberAssociationRegistrationService associationRegistrationService;

	public SignupService(SignUpValidator signUpValidator, MemberRepository memberRepository,
		MemberAssociationRegistrationService associationRegistrationService) {
		this.signUpValidator = signUpValidator;
		this.memberRepository = memberRepository;
		this.associationRegistrationService = associationRegistrationService;
	}

	@Transactional
	public void signup(Member member) {
		// 회원 정보 검증
		signUpValidator.validate(member);
		// 회원 저장
		memberRepository.save(member);
		// 회원 관련된 연관 데이터 등록
		associationRegistrationService.registerAll(member);
	}
}
