package co.fineants.api.domain.member.domain.entity;

import static co.fineants.api.domain.member.service.MemberService.*;

import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.global.errors.exception.business.EmailDuplicateException;

public class EmailDuplicationRule implements EmailValidationRule {

	private final MemberRepository memberRepository;

	public EmailDuplicationRule(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	@Override
	public void validate(String email) {
		if (memberRepository.findMemberByEmailAndProvider(email, LOCAL_PROVIDER).isPresent()) {
			throw new EmailDuplicateException(email);
		}
	}
}
