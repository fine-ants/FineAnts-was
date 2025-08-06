package co.fineants.api.domain.validator.domain.member;

import java.util.Arrays;
import java.util.List;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.validator.domain.MemberValidationRule;
import co.fineants.api.domain.validator.domain.Validator;

public class SignUpValidator implements Validator<Member> {
	private final List<MemberValidationRule> rules;

	public SignUpValidator(MemberValidationRule... rules) {
		this.rules = Arrays.asList(rules);
	}

	@Override
	public void validate(Member member) {
		for (MemberValidationRule rule : rules) {
			rule.validate(member);
		}
	}
}
