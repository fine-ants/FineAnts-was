package co.fineants.api.global.security.oauth.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import co.fineants.member.application.NicknameGenerator;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberEmail;
import co.fineants.member.domain.MemberProfile;
import co.fineants.member.domain.MemberRepository;
import co.fineants.member.domain.Nickname;
import co.fineants.member.domain.NotificationPreference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@RequiredArgsConstructor
@ToString
@Slf4j
public class OAuthAttribute {
	private final Map<String, Object> attributes;
	private final String nameAttributeKey;
	private final String email;
	private final String profileUrl;
	private final String provider;
	private final String sub;

	public static OAuthAttribute of(String provider, Map<String, Object> attributes,
		String nameAttributeKey) {
		switch (provider) {
			case "google" -> {
				return ofGoogle(attributes, nameAttributeKey);
			}
			case "kakao" -> {
				return ofKakao(attributes, nameAttributeKey);
			}
			case "naver" -> {
				return ofNaver(attributes, nameAttributeKey);
			}
			default -> throw new IllegalArgumentException("Invalid OAuth Provider");
		}
	}

	private static OAuthAttribute ofGoogle(Map<String, Object> attributes, String nameAttributeKey) {
		String email = (String)attributes.get("email");
		String profileUrl = (String)attributes.getOrDefault("picture", null);
		String provider = "google";
		String sub = (String)attributes.get("sub");
		return new OAuthAttribute(attributes, nameAttributeKey, email, profileUrl, provider, sub);
	}

	private static OAuthAttribute ofKakao(Map<String, Object> attributes, String nameAttributeKey) {
		log.info("attributes = {}", attributes);
		Map<String, Object> kakaoAccountMap = (Map<String, Object>)attributes.get("kakao_account");
		Map<String, Object> profileMap = new HashMap<>();
		if (kakaoAccountMap.containsKey("profile")) {
			profileMap = (Map<String, Object>)kakaoAccountMap.get("profile");
		}
		String email = (String)kakaoAccountMap.get("email");
		String profileUrl = (String)profileMap.getOrDefault("profile_image_url", null);
		String provider = "kakao";
		Long id = (Long)attributes.get("id");
		String sub = id.toString();
		return new OAuthAttribute(attributes, nameAttributeKey, email, profileUrl, provider, sub);
	}

	private static OAuthAttribute ofNaver(Map<String, Object> attributes, String nameAttributeKey) {
		Map<String, Object> responseMap = (Map<String, Object>)attributes.get("response");
		String email = (String)responseMap.get("email");
		String profileUrl = (String)responseMap.getOrDefault("profile_image", null);
		String sub = (String)responseMap.get("id");
		return new OAuthAttribute(attributes, nameAttributeKey, email, profileUrl, "naver", sub);
	}

	public Optional<Member> findMember(MemberRepository repository) {
		MemberEmail memberEmail = new MemberEmail(email);
		return repository.findMemberByEmailAndProvider(memberEmail, provider)
			.stream()
			.findAny();
	}

	public void updateProfileUrlIfAbsent(Member member) {
		if (member.getProfileUrl().isEmpty()) {
			member.changeProfileUrl(profileUrl);
		}
	}

	public Member toEntity(NicknameGenerator generator) {
		MemberEmail memberEmail = new MemberEmail(this.email);
		Nickname nickname = generator.generate();
		MemberProfile profile = MemberProfile.oauthMemberProfile(memberEmail, nickname, provider, profileUrl);
		NotificationPreference notificationPreference = NotificationPreference.defaultSetting();
		return Member.createMember(profile, notificationPreference);
	}
}
