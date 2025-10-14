package co.fineants.role.infrastructure;

import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Repository;

import co.fineants.role.domain.Role;
import co.fineants.role.domain.RoleRepository;

@Repository
public class RoleJpaRepository implements RoleRepository {

	private final co.fineants.role.infrastructure.RoleRepository repository;

	public RoleJpaRepository(co.fineants.role.infrastructure.RoleRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<Role> findRoleByRoleName(String roleName) {
		return repository.findRoleByRoleName(roleName);
	}

	@Override
	public Set<Role> findRolesByRoleNames(Set<String> roleNames) {
		return repository.findRolesByRoleNames(roleNames);
	}
}
