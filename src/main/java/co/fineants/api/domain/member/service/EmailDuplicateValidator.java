package co.fineants.api.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.member.repository.MemberRepository;

@Service
public class EmailDuplicateValidator {

	private final MemberRepository repository;

	public EmailDuplicateValidator(MemberRepository repository) {
		this.repository = repository;
	}

	@Transactional(readOnly = true)
	public boolean hasMemberWith(String email, String provider) {
		return repository.findMemberByEmailAndProvider(email, provider).isPresent();
	}
}
