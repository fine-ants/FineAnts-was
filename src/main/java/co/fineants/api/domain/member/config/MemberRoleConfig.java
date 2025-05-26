package co.fineants.api.domain.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.member.domain.factory.MemberRoleFactory;
import co.fineants.api.domain.member.service.RoleService;

@Configuration
public class MemberRoleConfig {
	@Bean
	public MemberRoleFactory memberRoleFactory(RoleService roleService) {
		return new MemberRoleFactory(roleService);
	}
}
