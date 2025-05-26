package co.fineants.api.domain.member.domain.rule;

import java.util.Arrays;
import java.util.List;

public class NicknameValidator {

	private final List<ValidationRule> rules;

	public NicknameValidator(ValidationRule... rules) {
		this.rules = Arrays.asList(rules);
	}

	public void validate(String nickname) {
		for (ValidationRule rule : rules) {
			rule.validate(nickname);
		}
	}
}
