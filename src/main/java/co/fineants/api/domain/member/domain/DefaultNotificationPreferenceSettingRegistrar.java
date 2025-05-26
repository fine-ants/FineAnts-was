package co.fineants.api.domain.member.domain;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.service.MemberNotificationPreferenceService;

public class DefaultNotificationPreferenceSettingRegistrar implements MemberAssociationRegistrar {

	private final MemberNotificationPreferenceService service;

	public DefaultNotificationPreferenceSettingRegistrar(MemberNotificationPreferenceService service) {
		this.service = service;
	}

	@Override
	public void register(Member member) {
		service.registerDefaultNotificationPreference(member);
	}
}
