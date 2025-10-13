package co.fineants.api.domain.member.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Embeddable
@Getter
@EqualsAndHashCode
public class Nickname {
	@Column(name = "nickname", nullable = false, unique = true, length = 100)
	private String value;

	protected Nickname() {
	}

	public Nickname(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Nickname=" + value;
	}
}
