package co.fineants.api.domain.member.service;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.MemberRole;
import co.fineants.api.domain.member.domain.entity.Role;
import co.fineants.api.domain.member.repository.RoleRepository;
import co.fineants.api.global.errors.exception.business.RoleNotFoundException;

public class MemberRoleFactory {

	private final RoleRepository roleRepository;

	public MemberRoleFactory(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}

	public MemberRole userMemberRole(Member member) {
		String roleName = "ROLE_USER";
		Role userRole = roleRepository.findRoleByRoleName(roleName)
			.orElseThrow(() -> new RoleNotFoundException(roleName));
		return new MemberRole(member, userRole);
	}
}
