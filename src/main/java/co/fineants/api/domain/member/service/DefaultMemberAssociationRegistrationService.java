package co.fineants.api.domain.member.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.registrar.MemberAssociationRegistrar;

public class DefaultMemberAssociationRegistrationService implements MemberAssociationRegistrationService {

	private final List<MemberAssociationRegistrar> registrars;

	public DefaultMemberAssociationRegistrationService(MemberAssociationRegistrar... registrars) {
		this.registrars = Arrays.asList(registrars);
	}

	@Override
	@Transactional
	public void registerAll(Member member) {
		for (MemberAssociationRegistrar registrar : registrars) {
			registrar.register(member);
		}
	}
}
