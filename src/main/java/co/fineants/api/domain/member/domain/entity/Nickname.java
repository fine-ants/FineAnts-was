package co.fineants.api.domain.member.domain.entity;

import java.util.regex.Pattern;

import org.apache.logging.log4j.util.Strings;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Embeddable
@Getter
@EqualsAndHashCode
public class Nickname {
	private static final Pattern PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9]{2,100}$");

	@Column(name = "nickname", nullable = false, unique = true, length = 100)
	private String value;

	protected Nickname() {
	}

	public Nickname(String value) {
		if (Strings.isBlank(value)) {
			throw new IllegalArgumentException("Nickname cannot be empty!");
		}
		if (!PATTERN.matcher(value).matches()) {
			throw new IllegalArgumentException("Nickname format is invalid!");
		}
		this.value = value;
	}

	@Override
	public String toString() {
		return "Nickname=" + value;
	}
}
