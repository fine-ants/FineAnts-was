package co.fineants.api.domain.validator.domain.member;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.MemberEmail;
import co.fineants.api.domain.validator.domain.MemberValidationRule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmailFormatRule implements MemberValidationRule {

	@Override
	public void validate(String email) {
		MemberEmail memberEmail = new MemberEmail(email);
		log.info("Validated email: {}", memberEmail);
	}

	@Override
	public void validate(Member member) {
		validate(member.getEmail());
	}
}
