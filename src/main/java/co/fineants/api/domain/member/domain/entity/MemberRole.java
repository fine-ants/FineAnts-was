package co.fineants.api.domain.member.domain.entity;

import org.springframework.security.core.GrantedAuthority;

import co.fineants.api.domain.role.domain.Role;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_role")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"member", "role"})
@Getter
public class MemberRole {
	@EmbeddedId
	private MemberRoleId id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	private Role role;

	public MemberRole(Member member, Role role) {
		this.member = member;
		this.role = role;
	}

	public static MemberRole of(Member member, Role role) {
		return new MemberRole(member, role);
	}

	//** 연관 관계 메서드 시작 **//
	public void setMember(Member member) {
		if (this.member != null) {
			this.member.removeMemberRole(this);
		}
		this.member = member;
		if (member != null) {
			member.addMemberRole(this);
		}
	}
	//** 연관 관계 메서드 종료 **//

	public GrantedAuthority toSimpleGrantedAuthority() {
		return role.toSimpleGrantedAuthority();
	}

	public String getRoleName() {
		return role.getRoleName();
	}

	@Override
	public String toString() {
		return String.format("MemberRole(id=%d, member=%s, role=%s)", id, member.getNickname(), role.getRoleName());
	}
}
