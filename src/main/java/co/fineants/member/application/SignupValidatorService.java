package co.fineants.member.application;

import org.springframework.stereotype.Service;

import co.fineants.api.domain.validator.domain.member.EmailValidator;
import co.fineants.api.domain.validator.domain.member.NicknameValidator;
import co.fineants.api.domain.validator.domain.member.PasswordValidator;
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
