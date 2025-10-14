package co.fineants.api.global.security.oauth.dto;

import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.role.infrastructure.RoleRepository;
import co.fineants.member.domain.Member;
import co.fineants.role.domain.Role;

class MemberAuthenticationTest extends AbstractContainerBaseTest {

	@Autowired
	private RoleRepository roleRepository;

	@DisplayName("Member를 MemberAuthentication으로 변환한다")
	@Test
	void from() {
		// given
		Member member = createMember();
		Set<String> roleNames = roleRepository.findAllById(member.getRoleIds()).stream()
			.map(Role::getRoleName)
			.collect(Collectors.toSet());

		// when
		MemberAuthentication authentication = MemberAuthentication.from(member, roleNames);
		// then
		Assertions.assertThat(authentication.toString())
			.hasToString("MemberAuthentication(id=null, nickname=nemo1234, "
				+ "email=dragonbead95@naver.com, roles=[ROLE_USER])");
	}

}
