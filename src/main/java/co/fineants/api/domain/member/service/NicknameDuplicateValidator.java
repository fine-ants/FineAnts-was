package co.fineants.api.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.member.domain.Nickname;
import co.fineants.api.domain.member.repository.MemberRepository;

@Service
public class NicknameDuplicateValidator {

	private final MemberRepository memberRepository;

	public NicknameDuplicateValidator(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	/**
	 * 닉네임 중복 검증
	 *
	 * @param nickname 닉네임
	 * @return true: 중복, false: 중복 아님
	 */
	@Transactional(readOnly = true)
	public boolean isDuplicate(Nickname nickname) {
		return memberRepository.findMemberByNickname(nickname).isPresent();
	}
}
