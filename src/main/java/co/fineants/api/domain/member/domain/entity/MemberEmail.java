package co.fineants.api.domain.member.domain.entity;

import java.util.regex.Pattern;

import org.apache.logging.log4j.util.Strings;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Embeddable
@Getter
@EqualsAndHashCode(of = "value")
@ToString
public class MemberEmail {
	private static final String PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";

	@Column(name = "email", nullable = false)
	private String value;

	protected MemberEmail() {
	}

	public MemberEmail(String value) {
		if (Strings.isBlank(value)) {
			throw new IllegalArgumentException("Email must not be blank");
		}
		if (value.contains("..")) {
			throw new IllegalArgumentException("Email must not contain consecutive dots");
		}
		if (!Pattern.matches(PATTERN, value)) {
			throw new IllegalArgumentException("Email format is invalid");
		}
		this.value = value;
	}
}
