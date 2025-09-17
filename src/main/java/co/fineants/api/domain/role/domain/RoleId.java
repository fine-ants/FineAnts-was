package co.fineants.api.domain.role.domain;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Embeddable
public class RoleId {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "role_id")
	private Long id;

	protected RoleId() {
	}

	public RoleId(Long id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object == null || getClass() != object.getClass())
			return false;
		RoleId roleId = (RoleId)object;
		return Objects.equals(id, roleId.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
