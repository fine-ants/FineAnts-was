package co.fineants.member.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.global.errors.exception.business.MemberNotFoundException;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.member.presentation.dto.response.ProfileResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReadMemberProfile {

	private final MemberRepository memberRepository;

	@Transactional(readOnly = true)
	public ProfileResponse read(Long memberId) {
		Member member = findMember(memberId);
		return ProfileResponse.from(member);
	}

	private Member findMember(Long id) {
		return memberRepository.findById(id)
			.orElseThrow(() -> new MemberNotFoundException(id.toString()));
	}
}
