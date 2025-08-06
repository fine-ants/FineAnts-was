package co.fineants.api.domain.member.domain.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;

import co.fineants.api.domain.BaseEntity;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import co.fineants.api.domain.validator.domain.MemberValidationRule;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "member", orphanRemoval = true, cascade = CascadeType.ALL)
	private NotificationPreference notificationPreference;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "member", orphanRemoval = true, cascade = CascadeType.ALL)
	private final Set<MemberRole> roles = new HashSet<>();

	private Member(MemberProfile profile) {
		this(null, profile);
	}

	private Member(Long id, MemberProfile profile) {
		this.id = id;
		this.profile = profile;
	}

	public static Member oauthMember(MemberProfile profile) {
		return new Member(profile);
	}

	public static Member localMember(MemberProfile profile) {
		return new Member(profile);
	}

	public static Member localMember(Long id, MemberProfile profile) {
		return new Member(id, profile);
	}

	//** 연관 관계 엔티티 메서드 시작 **//
	public void addMemberRole(MemberRole... memberRole) {
		for (MemberRole role : memberRole) {
			if (this.containsMemberRole(role)) {
				continue;
			}
			this.roles.add(role);
			if (role.getMember() != this) {
				role.setMember(this);
			}
		}
	}

	public void removeMemberRole(MemberRole memberRole) {
		this.roles.remove(memberRole);
		memberRole.setMember(null);
	}

	public boolean containsMemberRole(MemberRole memberRole) {
		for (MemberRole role : this.roles) {
			if (role.equals(memberRole)) {
				return true;
			}
		}
		return false;
	}

	public void setNotificationPreference(NotificationPreference notificationPreference) {
		if (this.notificationPreference != null) {
			this.notificationPreference.setMember(null);
		}
		this.notificationPreference = notificationPreference;
		if (notificationPreference != null && notificationPreference.getMember() != this) {
			notificationPreference.setMember(this);
		}
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

	public Collection<GrantedAuthority> getSimpleGrantedAuthorities() {
		return roles.stream()
			.map(MemberRole::toSimpleGrantedAuthority)
			.collect(Collectors.toSet());
	}

	public Map<String, Object> toAttributeMap() {
		Map<String, Object> result = new HashMap<>();
		result.put("id", id);
		result.putAll(profile.toMap());
		result.put("roles", roles.stream()
			.map(MemberRole::getRoleName)
			.collect(Collectors.toUnmodifiableSet()));
		return result;
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

	public Set<MemberRole> getRoles() {
		return Collections.unmodifiableSet(roles);
	}

	public void validateEmail(MemberValidationRule rule) {
		profile.validateEmail(rule);
	}

	public void validateNickname(MemberValidationRule rule) {
		profile.validateNickname(rule);
	}

	public void validateRules(MemberValidationRule... rules) {
		for (MemberValidationRule rule : rules) {
			rule.validate(this);
		}
	}

	@Override
	public String toString() {
		return String.format("Member(id=%d, nickname=%s, email=%s, roles=%s)", id, getNickname(), getEmail(),
			getSimpleGrantedAuthorities());
	}
}
