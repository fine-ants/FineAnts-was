package co.fineants.api.domain.member.domain.rule;

import java.util.Arrays;
import java.util.List;

public class EmailValidator implements Validator<String> {
	private final List<ValidationRule> rules;

	public EmailValidator(ValidationRule... rules) {
		this.rules = Arrays.asList(rules);
	}

	@Override
	public void validate(String email) {
		for (ValidationRule rule : rules) {
			rule.validate(email);
		}
	}
}
