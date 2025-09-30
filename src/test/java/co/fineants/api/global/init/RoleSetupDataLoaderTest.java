package co.fineants.api.global.init;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.member.repository.RoleRepository;
import co.fineants.api.domain.role.domain.Role;
import co.fineants.api.global.init.properties.RoleProperties;

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

}
