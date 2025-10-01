package co.fineants.api.domain.validator.domain.member;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.validator.domain.MemberValidationRule;
import co.fineants.api.global.errors.exception.business.NicknameDuplicateException;

public class NicknameDuplicationRule implements MemberValidationRule {

	private final MemberRepository memberRepository;

	public NicknameDuplicationRule(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	@Override
	public void validate(String nickname) {
		if (memberRepository.findMemberByNickname(nickname).isPresent()) {
			throw new NicknameDuplicateException(nickname);
		}
	}

	@Override
	public void validate(Member member) {
		validate(member.getNickname());
	}
}
