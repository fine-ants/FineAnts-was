package co.fineants.api.domain.validator.member;

import java.util.Arrays;
import java.util.List;

import co.fineants.api.domain.validator.MemberValidationRule;
import co.fineants.api.domain.validator.Validator;

public class NicknameValidator implements Validator<String> {

	private final List<MemberValidationRule> rules;

	public NicknameValidator(MemberValidationRule... rules) {
		this.rules = Arrays.asList(rules);
	}

	public void validate(String nickname) {
		for (MemberValidationRule rule : rules) {
			rule.validate(nickname);
		}
	}
}
