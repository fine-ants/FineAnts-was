package co.fineants.api.global.common.authorized.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.member.domain.Member;
import co.fineants.member.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberAuthorizedService implements AuthorizedService<Member> {

	private final MemberRepository memberRepository;

	@Override
	public List<Member> findResourceAllBy(List<Long> ids) {
		return memberRepository.findAllById(ids);
	}

	@Override
	public boolean isAuthorized(Object resource, Long memberId) {
		return ((Member)resource).hasAuthorization(memberId);
	}
}
