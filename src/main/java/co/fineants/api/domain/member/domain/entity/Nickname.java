package co.fineants.api.domain.member.domain.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Nickname {
	private final String value;

	public Nickname(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Nickname=" + value;
	}
}
