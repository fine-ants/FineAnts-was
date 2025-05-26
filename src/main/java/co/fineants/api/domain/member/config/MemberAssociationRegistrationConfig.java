package co.fineants.api.domain.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.member.domain.DefaultNotificationPreferenceSettingRegistrar;
import co.fineants.api.domain.member.domain.MemberAssociationRegistrar;
import co.fineants.api.domain.member.domain.MemberRoleRegistrar;
import co.fineants.api.domain.member.repository.MemberRoleRepository;
import co.fineants.api.domain.member.service.DefaultMemberAssociationRegistrationService;
import co.fineants.api.domain.member.service.MemberNotificationPreferenceService;
import co.fineants.api.domain.member.service.MemberRoleFactory;

@Configuration
public class MemberAssociationRegistrationConfig {

	@Bean
	public MemberRoleRegistrar memberRoleRegistrar(
		MemberRoleFactory memberRoleFactory,
		MemberRoleRepository memberRoleRepository) {
		return new MemberRoleRegistrar(memberRoleFactory, memberRoleRepository);
	}

	@Bean
	public DefaultNotificationPreferenceSettingRegistrar defaultNotificationPreferenceSettingRegister(
		MemberNotificationPreferenceService service) {
		return new DefaultNotificationPreferenceSettingRegistrar(service);
	}

	@Bean
	public DefaultMemberAssociationRegistrationService defaultMemberAssociationRegistrationService(
		MemberRoleRegistrar memberRoleRegistrar,
		DefaultNotificationPreferenceSettingRegistrar defaultNotificationPreferenceSettingRegistrar) {
		MemberAssociationRegistrar[] registrars = new MemberAssociationRegistrar[] {
			memberRoleRegistrar,
			defaultNotificationPreferenceSettingRegistrar
		};
		return new DefaultMemberAssociationRegistrationService(registrars);
	}
}
