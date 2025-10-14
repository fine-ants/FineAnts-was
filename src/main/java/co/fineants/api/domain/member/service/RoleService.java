package co.fineants.api.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.role.infrastructure.RoleRepository;
import co.fineants.api.global.errors.exception.business.RoleNotFoundException;
import co.fineants.role.domain.Role;

@Service
public class RoleService {
	private final RoleRepository roleRepository;

	public RoleService(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}

	@Transactional(readOnly = true)
	public Role findRole(String roleName) {
		return roleRepository.findRoleByRoleName(roleName)
			.orElseThrow(() -> new RoleNotFoundException(roleName));
	}
}
