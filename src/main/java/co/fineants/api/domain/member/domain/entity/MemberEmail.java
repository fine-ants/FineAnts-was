package co.fineants.api.domain.member.domain.entity;

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
	}

	@Override
	public String toString() {
		return "Email=" + value;
	}
}
