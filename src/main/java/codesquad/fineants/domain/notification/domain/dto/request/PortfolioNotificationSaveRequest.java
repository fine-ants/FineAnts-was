package codesquad.fineants.domain.notification.domain.dto.request;

import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.notification.domain.dto.response.NotifyMessage;
import codesquad.fineants.domain.notification.domain.dto.response.PortfolioNotifyMessage;
import codesquad.fineants.domain.notification.domain.entity.Notification;
import codesquad.fineants.domain.notification.domain.entity.type.NotificationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class PortfolioNotificationSaveRequest extends NotificationSaveRequest {
	private String name;
	private String title;
	private NotificationType type;
	private String referenceId;
	private String link;
	private Long memberId;

	public static PortfolioNotificationSaveRequest from(NotifyMessage message) {
		PortfolioNotifyMessage portfolioNotifyMessage = (PortfolioNotifyMessage)message;
		return PortfolioNotificationSaveRequest.builder()
			.name(portfolioNotifyMessage.getName())
			.title(portfolioNotifyMessage.getTitle())
			.type(portfolioNotifyMessage.getType())
			.referenceId(portfolioNotifyMessage.getReferenceId())
			.link(portfolioNotifyMessage.getLink())
			.memberId(portfolioNotifyMessage.getMemberId())
			.build();
	}

	@Override
	public Notification toEntity(Member member) {
		return Notification.portfolio(name, title, type, referenceId, link, member);
	}
}
