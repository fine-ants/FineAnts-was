package co.fineants.api.domain.member.domain.rule;

import co.fineants.api.domain.member.domain.entity.Member;

public interface ValidationRule {
	void validate(String text);

	void validate(Member member);
}
