package co.fineants.role.infrastructure;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import co.fineants.role.domain.Role;
import co.fineants.role.domain.RoleRepository;

@Repository
public class RoleJpaRepository implements RoleRepository {

	private final RoleSpringDataJpaRepository repository;

	public RoleJpaRepository(RoleSpringDataJpaRepository repository) {
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

	@Override
	public Set<Role> findAllById(Set<Long> roleIds) {
		return repository.findAllById(roleIds).stream()
			.collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public Set<Role> findAll() {
		return repository.findAll().stream()
			.collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public void save(Role role) {
		repository.save(role);
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}
}
