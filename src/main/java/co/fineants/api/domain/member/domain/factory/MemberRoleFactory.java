package co.fineants.api.domain.member.domain.factory;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.MemberRole;
import co.fineants.api.domain.member.service.RoleService;
import co.fineants.api.domain.role.domain.Role;

public class MemberRoleFactory {

	private final RoleService roleService;

	public MemberRoleFactory(RoleService roleService) {
		this.roleService = roleService;
	}

	public MemberRole userMemberRole(Member member) {
		String roleName = "ROLE_USER";
		Role userRole = roleService.findRole(roleName);
		return MemberRole.of(member, userRole);
	}
}
