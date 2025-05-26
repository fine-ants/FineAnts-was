package co.fineants.api.domain.member.domain.rule;

import java.util.Arrays;
import java.util.List;

import co.fineants.api.domain.member.domain.entity.Member;

public class SignUpValidator implements Validator<Member> {
	private final List<ValidationRule> rules;

	public SignUpValidator(ValidationRule... rules) {
		this.rules = Arrays.asList(rules);
	}

	@Override
	public void validate(Member member) {
		for (ValidationRule rule : rules) {
			rule.validate(member);
		}
	}
}
