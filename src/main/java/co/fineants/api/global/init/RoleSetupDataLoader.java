package co.fineants.api.global.init;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.member.repository.RoleRepository;
import co.fineants.api.domain.role.domain.Role;
import co.fineants.api.global.init.properties.RoleProperties;

@Service
public class RoleSetupDataLoader {

	private final RoleRepository roleRepository;

	public RoleSetupDataLoader(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}

	@Transactional
	public void setupRoles(RoleProperties roleProperties) {
		if (roleProperties == null || roleProperties.getProperties() == null) {
			return;
		}
		for (RoleProperties.RoleProperty roleProperty : roleProperties.getProperties()) {
			Role role = findOrCreateRole(roleProperty);
			roleRepository.save(role);
		}
	}

	private Role findOrCreateRole(RoleProperties.RoleProperty roleProperty) {
		return roleRepository.findRoleByRoleName(roleProperty.getRoleName())
			.orElseGet(roleProperty::toRoleEntity);
	}
}
