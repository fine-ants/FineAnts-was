package co.fineants.api.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.member.domain.MemberEmail;
import co.fineants.member.infrastructure.MemberRepository;

@Service
public class EmailDuplicateValidator {

	private final MemberRepository repository;

	public EmailDuplicateValidator(MemberRepository repository) {
		this.repository = repository;
	}

	@Transactional(readOnly = true)
	public boolean hasMemberWith(MemberEmail email, String provider) {
		return repository.findMemberByEmailAndProvider(email, provider).isPresent();
	}
}
