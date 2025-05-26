package co.fineants.api.domain.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.member.domain.DefaultNotificationPreferenceSettingRegister;
import co.fineants.api.domain.member.domain.MemberRoleRegistrar;
import co.fineants.api.domain.member.repository.MemberRoleRepository;
import co.fineants.api.domain.member.service.MemberNotificationPreferenceService;
import co.fineants.api.domain.member.service.MemberRoleFactory;
import co.fineants.api.domain.member.service.RoleService;

@Configuration
public class MemberRoleConfig {
	@Bean
	public MemberRoleFactory memberRoleFactory(RoleService roleService) {
		return new MemberRoleFactory(roleService);
	}

	@Bean
	public MemberRoleRegistrar memberRoleRegistrar(
		MemberRoleFactory memberRoleFactory,
		MemberRoleRepository memberRoleRepository) {
		return new MemberRoleRegistrar(memberRoleFactory, memberRoleRepository);
	}

	@Bean
	public DefaultNotificationPreferenceSettingRegister defaultNotificationPreferenceSettingRegister(
		MemberNotificationPreferenceService service) {
		return new DefaultNotificationPreferenceSettingRegister(service);
	}
}
