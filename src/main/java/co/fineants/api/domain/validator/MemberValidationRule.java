package co.fineants.api.domain.validator;

import co.fineants.api.domain.member.domain.entity.Member;

public interface MemberValidationRule {
	void validate(String text);

	void validate(Member member);
}
