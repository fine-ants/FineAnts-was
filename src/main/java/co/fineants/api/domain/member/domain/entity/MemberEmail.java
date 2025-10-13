package co.fineants.api.domain.member.domain.entity;

import java.util.regex.Pattern;

import org.apache.logging.log4j.util.Strings;

import co.fineants.api.global.errors.exception.business.EmailInvalidInputException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Embeddable
@Getter
@EqualsAndHashCode
public class MemberEmail {
	@Column(name = "email", nullable = false)
	private String value;

	protected MemberEmail() {
	}

	public MemberEmail(String value) {
		this.value = value;
		if (Strings.isBlank(this.value)) {
			throw new EmailInvalidInputException(this.value);
		}
		if (this.value.contains("..")) { // 연속된 마침표는 허용하지 않음
			throw new EmailInvalidInputException(this.value);
		}
		Pattern pattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
		if (!pattern.matcher(this.value).matches()) {
			throw new EmailInvalidInputException(value);
		}
	}

	@Override
	public String toString() {
		return "Email=" + value;
	}
}
