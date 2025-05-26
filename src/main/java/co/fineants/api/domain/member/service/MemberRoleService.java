package co.fineants.api.domain.member.service;

import org.springframework.stereotype.Service;

import co.fineants.api.domain.member.domain.entity.MemberRole;
import co.fineants.api.domain.member.repository.MemberRoleRepository;

@Service
public class MemberRoleService {
	private final MemberRoleRepository memberRoleRepository;

	public MemberRoleService(MemberRoleRepository memberRoleRepository) {
		this.memberRoleRepository = memberRoleRepository;
	}

	public void save(MemberRole memberRole) {
		memberRoleRepository.save(memberRole);
	}
}
