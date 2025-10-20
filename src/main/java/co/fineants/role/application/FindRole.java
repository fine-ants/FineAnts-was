package co.fineants.role.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.role.domain.Role;
import co.fineants.role.domain.RoleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindRole {

	private final RoleRepository roleRepository;

	@Transactional(readOnly = true)
	public Role findBy(String roleName) {
		return roleRepository.findRoleByRoleName(roleName)
			.orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
	}

}
