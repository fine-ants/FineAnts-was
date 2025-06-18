package co.fineants.api.domain.validator.member;

import java.util.Arrays;
import java.util.List;

import co.fineants.api.domain.validator.MemberValidationRule;
import co.fineants.api.domain.validator.Validator;

public class EmailValidator implements Validator<String> {
	private final List<MemberValidationRule> rules;

	public EmailValidator(MemberValidationRule... rules) {
		this.rules = Arrays.asList(rules);
	}

	@Override
	public void validate(String email) {
		for (MemberValidationRule rule : rules) {
			rule.validate(email);
		}
	}
}
