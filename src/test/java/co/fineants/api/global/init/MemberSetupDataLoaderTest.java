package co.fineants.api.global.init;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.member.repository.RoleRepository;
import co.fineants.api.domain.role.domain.Role;
import co.fineants.api.global.init.properties.MemberProperties;

class MemberSetupDataLoaderTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberSetupDataLoader loader;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private MemberProperties memberProperties;

	@BeforeEach
	void setUp() {
		memberRepository.deleteAll();
	}

	@AfterEach
	void tearDown() {
		memberRepository.deleteAll();
	}

	@Test
	@Transactional
	void setupMembers() {
		loader.setupMembers(memberProperties);

		Assertions.assertThat(memberRepository.findAll())
			.hasSize(3);

		Role userRole = roleRepository.findRoleByRoleName("ROLE_USER").orElseThrow();
		Role managerRole = roleRepository.findRoleByRoleName("ROLE_MANAGER").orElseThrow();
		Role adminRole = roleRepository.findRoleByRoleName("ROLE_ADMIN").orElseThrow();
		Assertions.assertThat(memberRepository.findAll().get(0).getRoleIds())
			.contains(userRole.getId());
		Assertions.assertThat(memberRepository.findAll().get(1).getRoleIds())
			.contains(managerRole.getId());
		Assertions.assertThat(memberRepository.findAll().get(2).getRoleIds())
			.contains(adminRole.getId());
	}
}
