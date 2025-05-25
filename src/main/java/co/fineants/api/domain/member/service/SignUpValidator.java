package co.fineants.api.domain.member.service;

import co.fineants.api.domain.member.domain.entity.Member;

public interface SignUpValidator {
	void validate(Member member);
}
