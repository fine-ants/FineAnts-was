package co.fineants.api.domain.validator.member;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.validator.MemberValidationRule;
import co.fineants.api.global.errors.exception.business.EmailDuplicateException;

public class EmailDuplicationRule implements MemberValidationRule {

	private final MemberRepository memberRepository;
	private final String localProvider;

	public EmailDuplicationRule(MemberRepository memberRepository, String localProvider) {
		this.memberRepository = memberRepository;
		this.localProvider = localProvider;
	}

	@Override
	public void validate(String email) {
		if (memberRepository.findMemberByEmailAndProvider(email, localProvider).isPresent()) {
			throw new EmailDuplicateException(email);
		}
	}

	@Override
	public void validate(Member member) {
		member.validateEmail(this);
	}
}
