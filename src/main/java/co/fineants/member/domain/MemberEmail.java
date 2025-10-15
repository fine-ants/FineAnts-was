package co.fineants.member.domain;

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
	public static final String EMAIL_REGEXP = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
	private static final Pattern PATTERN = Pattern.compile(EMAIL_REGEXP);

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
		if (!PATTERN.matcher(value).matches()) {
			throw new IllegalArgumentException("Email format is invalid");
		}
		this.value = value;
	}
}
