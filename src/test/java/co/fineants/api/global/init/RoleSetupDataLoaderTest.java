package co.fineants.api.global.init;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.role.infrastructure.RoleRepository;
import co.fineants.api.global.init.properties.RoleProperties;
import co.fineants.role.domain.Role;

class RoleSetupDataLoaderTest extends AbstractContainerBaseTest {

	@Autowired
	private RoleSetupDataLoader loader;

	@Autowired
	private RoleProperties roleProperties;

	@Autowired
	private RoleRepository repository;

	@BeforeEach
	void setUp() {
		repository.deleteAll();
	}

	@AfterEach
	void tearDown() {
		repository.deleteAll();
	}

	@Test
	void setupRoles() {
		loader.setupRoles(roleProperties);

		Role userRole = Role.create("ROLE_USER", "회원");
		Role managerRole = Role.create("ROLE_MANAGER", "매니저");
		Role adminRole = Role.create("ROLE_ADMIN", "관리자");
		Assertions.assertThat(repository.findAll())
			.hasSize(3)
			.containsExactlyInAnyOrder(userRole, managerRole, adminRole);
	}

	@Test
	void setupRoles_whenTwice_thenNotDuplicateSavedRoles() {
		loader.setupRoles(roleProperties);
		loader.setupRoles(roleProperties);

		Role userRole = Role.create("ROLE_USER", "회원");
		Role managerRole = Role.create("ROLE_MANAGER", "매니저");
		Role adminRole = Role.create("ROLE_ADMIN", "관리자");
		Assertions.assertThat(repository.findAll())
			.hasSize(3)
			.containsExactlyInAnyOrder(userRole, managerRole, adminRole);
	}

	@Test
	void setupRoles_whenRolePropertiesIsNull_thenNoRolesSaved() {
		loader.setupRoles(null);

		Assertions.assertThat(repository.findAll()).isEmpty();
	}
}
