package co.fineants.api.domain.member.domain.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import co.fineants.api.domain.BaseEntity;
import co.fineants.api.domain.validator.domain.MemberValidationRule;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EqualsAndHashCode(of = {"profile"}, callSuper = false)
public class Member extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Embedded
	private MemberProfile profile;

	@Embedded
	private NotificationPreference notificationPreference;

	@ElementCollection
	@CollectionTable(
		name = "member_role",
		joinColumns = @JoinColumn(name = "member_id")
	)
	@Column(name = "role_id")
	private final Set<Long> roleIds = new HashSet<>();

	public static Member createMember(MemberProfile profile, NotificationPreference notificationPreference) {
		return new Member(profile, notificationPreference);
	}

	private Member(MemberProfile profile, NotificationPreference notificationPreference) {
		setMemberProfile(profile);
		setNotificationPreference(notificationPreference);
	}

	private void setMemberProfile(MemberProfile profile) {
		if (profile == null) {
			throw new IllegalArgumentException("MemberProfile must not be null");
		}
		this.profile = profile;
	}

	public void setNotificationPreference(NotificationPreference notificationPreference) {
		this.notificationPreference = notificationPreference;
	}

	//** 연관 관계 엔티티 메서드 시작 **//
	public void addRoleId(Long roleId) {
		this.roleIds.add(roleId);
	}

	public void addRoleIds(Collection<Long> roleIds) {
		this.roleIds.addAll(roleIds);
	}

	public void removeRoleId(Long roleId) {
		this.roleIds.remove(roleId);
	}

	public boolean containsRoleId(Long roleId) {
		return this.roleIds.contains(roleId);
	}

	//** 연관 관계 엔티티 메서드 종료 **//
	public boolean hasAuthorization(Long memberId) {
		return id.equals(memberId);
	}

	public void changeProfileUrl(String profileUrl) {
		profile.changeProfileUrl(profileUrl);
	}

	public void changeNickname(String nickname) {
		this.profile.changeNickname(nickname);
	}

	public Optional<String> getPassword() {
		return profile.getPassword();
	}

	public String getProvider() {
		return profile.getProvider();
	}

	public String getNickname() {
		return profile.getNickname();
	}

	public String getEmail() {
		return profile.getEmail();
	}

	public Optional<String> getProfileUrl() {
		return profile.getProfileUrl();
	}

	public void validateNickname(MemberValidationRule rule) {
		profile.validateNickname(rule);
	}

	@Override
	public String toString() {
		return String.format("Member(id=%d, nickname=%s, email=%s, roleIds=%s)", id, getNickname(), getEmail(),
			roleIds);
	}
}
