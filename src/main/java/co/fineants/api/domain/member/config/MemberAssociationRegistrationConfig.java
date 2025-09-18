package co.fineants.api.domain.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.member.domain.registrar.DefaultNotificationPreferenceSettingRegistrar;
import co.fineants.api.domain.member.service.MemberNotificationPreferenceService;

@Configuration
public class MemberAssociationRegistrationConfig {

	@Bean
	public DefaultNotificationPreferenceSettingRegistrar defaultNotificationPreferenceSettingRegister(
		MemberNotificationPreferenceService service) {
		return new DefaultNotificationPreferenceSettingRegistrar(service);
	}
}
