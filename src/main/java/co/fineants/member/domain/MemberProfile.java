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
	@Embedded
	private MemberPassword password;

	@Column(name = "profile_url")
	private String profileUrl;

	public MemberProfile(MemberEmail email, Nickname nickname, String provider, MemberPassword password,
		String profileUrl) {
		this.email = email;
		this.nickname = nickname;
		this.provider = provider;
		this.password = password;
		this.profileUrl = profileUrl;
	}

	public static MemberProfile oauthMemberProfile(MemberEmail email, Nickname nickname, String provider,
		String profileUrl) {
		MemberPassword password = null;
		return new MemberProfile(email, nickname, provider, password, profileUrl);
	}

	public static MemberProfile localMemberProfile(MemberEmail email, Nickname nickname, MemberPassword memberPassword,
		String profileUrl) {
		return new MemberProfile(email, nickname, "local", memberPassword, profileUrl);
	}

	public void changeNickname(Nickname nickname) {
		this.nickname = nickname;
	}

	public void changeProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}

	public void changePassword(MemberPassword password) {
		this.password = password;
	}

	public Map<String, Object> toMap() {
		return Map.ofEntries(
			Map.entry("email", email.getValue()),
			Map.entry("nickname", nickname.getValue()),
			Map.entry("provider", provider),
			Map.entry("profileUrl", profileUrl)
		);
	}

	public Optional<String> getPassword() {
		return Optional.ofNullable(password.getValue());
	}

	public Optional<String> getProfileUrl() {
		return Optional.ofNullable(profileUrl);
	}
}
