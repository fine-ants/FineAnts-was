package co.fineants.member.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import co.fineants.member.domain.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"id", "nickname", "email", "profileUrl"})
public class OauthMemberResponse {
	private Long id;
	private String nickname;
	private String email;
	private String profileUrl;
	private String provider;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private MemberNotificationPreferenceResponse notificationPreferences;

	public OauthMemberResponse(Long id, String nickname, String email, String profileUrl, String provider,
		MemberNotificationPreferenceResponse notificationPreferences) {
		this.id = id;
		this.nickname = nickname;
		this.email = email;
		this.profileUrl = profileUrl;
		this.provider = provider;
		this.notificationPreferences = notificationPreferences;
	}

	public static OauthMemberResponse from(Member member) {
		return from(member, null);
	}

	public static OauthMemberResponse from(Member member, MemberNotificationPreferenceResponse response) {
		return new OauthMemberResponse(
			member.getId(),
			member.getNickname(),
			member.getEmail(),
			member.getProfileUrl().orElse(null),
			member.getProvider(),
			response);
	}

	@Override
	public String toString() {
		return String.format("%s, %s(id=%d, nickname=%s, email=%s, profileUrl=%s)", "로그인 회원정보 응답",
			this.getClass().getSimpleName(), id, nickname, email, profileUrl);
	}
}
