package co.fineants.api.domain.member.service;

import static co.fineants.api.domain.member.service.MemberService.*;

import org.apache.logging.log4j.util.Strings;

import co.fineants.api.domain.member.domain.entity.MemberProfile;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.global.errors.exception.business.EmailDuplicateException;
import co.fineants.api.global.errors.exception.business.EmailInvalidInputException;

public class SignUpEmailValidator implements SignUpValidator {

	private final MemberRepository memberRepository;

	public SignUpEmailValidator(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	@Override
	public void validate(String email) throws EmailInvalidInputException, EmailDuplicateException {
		if (Strings.isBlank(email)) {
			throw new EmailInvalidInputException(email);
		}
		if (!email.matches(MemberProfile.EMAIL_REGEXP)) {
			throw new EmailInvalidInputException(email);
		}
		if (memberRepository.findMemberByEmailAndProvider(email, LOCAL_PROVIDER).isPresent()) {
			throw new EmailDuplicateException(email);
		}
	}
}
