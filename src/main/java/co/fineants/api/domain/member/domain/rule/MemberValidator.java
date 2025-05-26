package co.fineants.api.domain.member.domain.rule;

import co.fineants.api.domain.member.domain.entity.Member;

public interface MemberValidator {
	void validate(Member member);
}
