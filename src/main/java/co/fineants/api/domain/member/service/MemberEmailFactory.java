package co.fineants.api.domain.member.service;

import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import co.fineants.api.domain.member.domain.entity.MemberEmail;
import co.fineants.api.domain.member.properties.EmailProperties;
import co.fineants.api.global.errors.exception.business.EmailInvalidInputException;

@Component
public class MemberEmailFactory {

	private final EmailProperties emailProperties;

	public MemberEmailFactory(EmailProperties emailProperties) {
		this.emailProperties = emailProperties;
	}

	public MemberEmail create(String value) {
		if (Strings.isBlank(value)) { // 빈 문자열이나 null은 허용하지 않음
			throw new EmailInvalidInputException(value);
		}
		if (value.contains("..")) { // 연속된 마침표는 허용하지 않음
			throw new EmailInvalidInputException(value);
		}
		if (!emailProperties.getEmailPattern().matcher(value).matches()) { // 정규 표현식으로 이메일 형식 검증
			throw new EmailInvalidInputException(value);
		}
		return new MemberEmail(value);
	}
}
