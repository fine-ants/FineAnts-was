package co.fineants.member.presentation.dto.response;

import co.fineants.member.domain.NotificationPreference;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class MemberNotificationPreferenceResponse {
	private Boolean browserNotify;
	private Boolean targetGainNotify;
	private Boolean maxLossNotify;
	private Boolean targetPriceNotify;

	public MemberNotificationPreferenceResponse(Boolean browserNotify, Boolean targetGainNotify, Boolean maxLossNotify,
		Boolean targetPriceNotify) {
		this.browserNotify = browserNotify;
		this.targetGainNotify = targetGainNotify;
		this.maxLossNotify = maxLossNotify;
		this.targetPriceNotify = targetPriceNotify;
	}

	public static MemberNotificationPreferenceResponse from(NotificationPreference notificationPreference) {
		return MemberNotificationPreferenceResponse.builder()
			.browserNotify(notificationPreference.isBrowserNotify())
			.targetGainNotify(notificationPreference.isTargetGainNotify())
			.maxLossNotify(notificationPreference.isMaxLossNotify())
			.targetPriceNotify(notificationPreference.isTargetPriceNotify())
			.build();
	}
}
