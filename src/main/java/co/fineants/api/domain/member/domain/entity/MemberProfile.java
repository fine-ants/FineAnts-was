package co.fineants.api.domain.member.domain.entity;

import java.util.Map;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = {"email", "provider"}, callSuper = false)
public class MemberProfile {
	public static final String EMAIL_REGEXP = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
	public static final String NICKNAME_REGEXP = "^[가-힣a-zA-Z0-9]{2,10}$";
	public static final String PASSWORD_REGEXP = "^(?=.*[a-zA-Z])(?=.*[\\d])(?=.*[!@#$%^&*]).{8,16}$";

	@Getter
	@Column(name = "email", nullable = false)
	private String email;
	@Getter
	@Column(name = "nickname", unique = true, nullable = false)
	private String nickname;
	@Getter
	@Column(name = "provider", nullable = false)
	private String provider;
	@Column(name = "password")
	private String password;
	@Column(name = "profile_url")
	private String profileUrl;

	public static MemberProfile oauthMemberProfile(String email, String nickname, String provider, String profileUrl) {
		return new MemberProfile(email, nickname, provider, null, profileUrl);
	}

	public static MemberProfile localMemberProfile(String email, String nickname, String password, String profileUrl) {
		return new MemberProfile(email, nickname, "local", password, profileUrl);
	}

	public void changeNickname(String nickname) {
		this.nickname = nickname;
	}

	public void changeProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}

	public Map<String, Object> toMap() {
		return Map.ofEntries(
			Map.entry("email", email),
			Map.entry("nickname", nickname),
			Map.entry("provider", provider),
			Map.entry("profileUrl", profileUrl)
		);
	}

	public Optional<String> getPassword() {
		return Optional.ofNullable(password);
	}

	public Optional<String> getProfileUrl() {
		return Optional.ofNullable(profileUrl);
	}

	public void validateEmail(ValidationRule rule) {
		rule.validate(email);
	}

	public void validateNickname(ValidationRule rule) {
		rule.validate(nickname);
	}
}
