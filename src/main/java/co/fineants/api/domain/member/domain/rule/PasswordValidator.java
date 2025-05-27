package co.fineants.api.domain.member.domain.rule;

import co.fineants.api.global.errors.exception.business.PasswordAuthenticationException;

public class PasswordValidator {

	public void validateMatch(String password, String passwordConfirm) {
		if (!password.equals(passwordConfirm)) {
			throw new PasswordAuthenticationException(password);
		}
	}
}
