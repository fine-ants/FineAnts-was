package co.fineants.api.domain.validator.domain;

import co.fineants.member.domain.Member;

public interface MemberValidationRule {
	void validate(String text);

	void validate(Member member);
}
