package co.fineants.api.domain.member.domain;

import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.MemberRole;
import co.fineants.api.domain.member.repository.MemberRoleRepository;
import co.fineants.api.domain.member.service.MemberRoleFactory;

public class MemberRoleRegistrar implements MemberAssociationRegistrar {

	private final MemberRoleFactory memberRoleFactory;
	private final MemberRoleRepository memberRoleRepository;

	public MemberRoleRegistrar(MemberRoleFactory memberRoleFactory, MemberRoleRepository memberRoleRepository) {
		this.memberRoleFactory = memberRoleFactory;
		this.memberRoleRepository = memberRoleRepository;
	}

	@Override
	@Transactional
	public void register(Member member) {
		MemberRole memberRole = memberRoleFactory.userMemberRole(member);
		memberRoleRepository.save(memberRole);
	}
}
