package co.fineants.api.domain.member.service;

import org.springframework.stereotype.Service;

import co.fineants.api.domain.member.domain.rule.EmailValidator;
import co.fineants.api.domain.member.domain.rule.NicknameValidator;
import co.fineants.api.domain.member.domain.rule.PasswordValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignupValidatorService {

	private final NicknameValidator nicknameValidator;
	private final EmailValidator emailValidator;
	private final PasswordValidator passwordValidator;

	public void validateNickname(String nickname) {
		nicknameValidator.validate(nickname);
	}

	public void validateEmail(String email) {
		emailValidator.validate(email);
	}

	public void validatePassword(String password, String passwordConfirm) {
		passwordValidator.validateMatch(password, passwordConfirm);
	}
}
