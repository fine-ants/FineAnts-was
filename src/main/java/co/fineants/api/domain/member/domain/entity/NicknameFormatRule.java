package co.fineants.api.domain.member.domain.entity;

import java.util.regex.Pattern;

import org.apache.logging.log4j.util.Strings;

import co.fineants.api.global.errors.exception.business.NicknameInvalidInputException;

public class NicknameFormatRule implements ValidationRule {

	private final Pattern nicknamePattern;

	public NicknameFormatRule(Pattern nicknamePattern) {
		this.nicknamePattern = nicknamePattern;
	}

	@Override
	public void validate(String nickname) {
		if (Strings.isBlank(nickname)) {
			throw new NicknameInvalidInputException(nickname);
		}
		if (!nicknamePattern.matcher(nickname).matches()) {
			throw new NicknameInvalidInputException(nickname);
		}
	}

	@Override
	public void validate(Member member) {
		member.validateNickname(this);
	}
}
