package co.fineants.role.domain;

import java.util.Optional;
import java.util.Set;

public interface RoleRepository {
	Optional<Role> findRoleByRoleName(String roleName);

	Set<Role> findRolesByRoleNames(Set<String> roleNames);

	Set<Role> findAllById(Set<Long> roleIds);

	Set<Role> findAll();

	Role save(Role role);

	void deleteAll();
}
