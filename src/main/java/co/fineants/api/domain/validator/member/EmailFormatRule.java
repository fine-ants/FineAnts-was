package co.fineants.api.domain.validator.member;

import java.util.regex.Pattern;

import org.apache.logging.log4j.util.Strings;

import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.validator.MemberValidationRule;
import co.fineants.api.global.errors.exception.business.EmailInvalidInputException;

public class EmailFormatRule implements MemberValidationRule {

	private final Pattern emailRegexp;

	public EmailFormatRule(Pattern emailRegexp) {
		this.emailRegexp = emailRegexp;
	}

	@Override
	public void validate(String email) {
		if (Strings.isBlank(email)) { // 빈 문자열이나 null은 허용하지 않음
			throw new EmailInvalidInputException(email);
		}
		if (email.contains("..")) { // 연속된 마침표는 허용하지 않음
			throw new EmailInvalidInputException(email);
		}
		if (!emailRegexp.matcher(email).matches()) { // 정규 표현식으로 이메일 형식 검증
			throw new EmailInvalidInputException(email);
		}
	}

	@Override
	public void validate(Member member) {
		member.validateEmail(this);
	}
}
