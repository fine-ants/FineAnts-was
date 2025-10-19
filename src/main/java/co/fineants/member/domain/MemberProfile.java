package co.fineants.member.domain;

import java.util.Map;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"email", "provider"}, callSuper = false)
public class MemberProfile {
	@Getter
	@Embedded
	private MemberEmail email;
	@Getter
	@Embedded
	private Nickname nickname;
	@Getter
	@Column(name = "provider", nullable = false)
	private String provider;
	@Column(name = "password")
	private String password;
	@Column(name = "profile_url")
	private String profileUrl;

	public MemberProfile(MemberEmail email, Nickname nickname, String provider, String password, String profileUrl) {
		this.email = email;
		this.nickname = nickname;
		this.provider = provider;
		this.password = password;
		this.profileUrl = profileUrl;
	}

	public static MemberProfile oauthMemberProfile(MemberEmail email, Nickname nickname, String provider,
		String profileUrl) {
		return new MemberProfile(email, nickname, provider, null, profileUrl);
	}

	public static MemberProfile localMemberProfile(MemberEmail email, Nickname nickname, String password,
		String profileUrl) {
		return new MemberProfile(email, nickname, "local", password, profileUrl);
	}

	public void changeNickname(Nickname nickname) {
		this.nickname = nickname;
	}

	public void changeProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}

	public void changePassword(String encodedPassword) {
		this.password = encodedPassword;
	}

	public Map<String, Object> toMap() {
		return Map.ofEntries(
			Map.entry("email", email),
			Map.entry("nickname", nickname.getValue()),
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
}
