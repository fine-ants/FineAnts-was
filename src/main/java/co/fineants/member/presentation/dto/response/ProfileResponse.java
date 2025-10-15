package co.fineants.member.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.member.domain.Member;
import co.fineants.member.domain.NotificationPreference;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ProfileResponse {

	@JsonProperty("user")
	private MemberProfile memberProfile;

	public static ProfileResponse from(Member member) {
		NotificationPreferenceDto preference = NotificationPreferenceDto.from(member.getNotificationPreference());
		MemberProfile memberProfile = MemberProfile.builder()
			.id(member.getId())
			.nickname(member.getNickname())
			.email(member.getEmail())
			.profileUrl(member.getProfileUrl().orElse(null))
			.provider(member.getProvider())
			.notificationPreferences(preference)
			.build();
		return new ProfileResponse(memberProfile);
	}

	@Getter
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@Builder
	public static class MemberProfile {
		private Long id;
		private String nickname;
		private String email;
		private String profileUrl;
		private String provider;
		private NotificationPreferenceDto notificationPreferences;
	}

	@Getter
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@Builder
	public static class NotificationPreferenceDto {
		private Boolean browserNotify;
		private Boolean targetGainNotify;
		private Boolean maxLossNotify;
		private Boolean targetPriceNotify;

		public static NotificationPreferenceDto from(NotificationPreference preference) {
			return NotificationPreferenceDto.builder()
				.browserNotify(preference.isBrowserNotify())
				.targetGainNotify(preference.isTargetGainNotify())
				.maxLossNotify(preference.isMaxLossNotify())
				.targetPriceNotify(preference.isTargetPriceNotify())
				.build();
		}
	}
}
