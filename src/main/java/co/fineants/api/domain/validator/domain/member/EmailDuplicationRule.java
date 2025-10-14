package co.fineants.api.domain.validator.domain.member;

import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberEmail;
import co.fineants.api.domain.member.service.EmailDuplicateValidator;
import co.fineants.api.domain.validator.domain.MemberValidationRule;
import co.fineants.api.global.errors.exception.business.EmailDuplicateException;

public class EmailDuplicationRule implements MemberValidationRule {

	private final EmailDuplicateValidator emailDuplicateValidator;
	private final String localProvider;

	public EmailDuplicationRule(EmailDuplicateValidator emailDuplicateValidator, String localProvider) {
		this.emailDuplicateValidator = emailDuplicateValidator;
		this.localProvider = localProvider;
	}

	@Override
	public void validate(String email) {
		MemberEmail memberEmail = new MemberEmail(email);
		if (emailDuplicateValidator.hasMemberWith(memberEmail, localProvider)) {
			throw new EmailDuplicateException(email);
		}
	}

	@Override
	public void validate(Member member) {
		validate(member.getEmail());
	}
}
