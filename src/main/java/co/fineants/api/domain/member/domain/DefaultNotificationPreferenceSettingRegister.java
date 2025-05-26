package co.fineants.api.domain.member.domain;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.service.MemberNotificationPreferenceService;

public class DefaultNotificationPreferenceSettingRegister implements MemberAssociationRegistrar {

	private final MemberNotificationPreferenceService service;

	public DefaultNotificationPreferenceSettingRegister(MemberNotificationPreferenceService service) {
		this.service = service;
	}

	@Override
	public void register(Member member) {
		service.registerDefaultNotificationPreference(member);
	}
}
