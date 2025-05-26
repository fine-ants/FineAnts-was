package co.fineants.api.domain.member.domain.entity;

import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.global.errors.exception.business.NicknameDuplicateException;

public class NicknameDuplicationRule implements ValidationRule {

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
		member.validateNickname(this);
	}
}
