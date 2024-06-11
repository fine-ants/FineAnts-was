package codesquad.fineants.global.security.oauth.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.member.service.NicknameGenerator;
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
		}
		throw new IllegalArgumentException("Invalid OAuth Provider");
	}

	private static OAuthAttribute ofGoogle(Map<String, Object> attributes, String nameAttributeKey) {
		String email = (String)attributes.get("email");
		String profileUrl = (String)attributes.get("picture");
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
		String profileUrl = (String)responseMap.get("profile_image");
		String sub = (String)responseMap.get("id");
		return new OAuthAttribute(attributes, nameAttributeKey, email, profileUrl, "naver", sub);
	}

	public Optional<Member> getMemberFrom(MemberRepository repository) {
		return repository.findMemberByEmailAndProvider(email, provider)
			.map(entity -> entity.updateProfileUrl(profileUrl))
			.stream().findAny();
	}

	public Member toEntity(NicknameGenerator generator) {
		String nickname = generator.generate();
		return Member.oauthMember(
			email,
			nickname,
			provider,
			profileUrl
		);
	}
}
