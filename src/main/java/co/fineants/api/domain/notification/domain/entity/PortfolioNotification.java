package co.fineants.api.domain.notification.domain.entity;

import co.fineants.api.domain.notification.domain.entity.type.NotificationType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("P")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
public class PortfolioNotification extends Notification {
	private String name;
	private Long portfolioId;

	@Override
	public Notification withId(Long id) {
		return this.toBuilder()
			.id(id)
			.build();
	}

	@Override
	public NotificationBody getBody() {
		return NotificationBody.portfolio(name, getType());
	}

	@Override
	public String getContent() {
		if (getType() == NotificationType.PORTFOLIO_TARGET_GAIN) {
			return String.format("%s의 목표 수익율을 달성했습니다", name);
		} else if (getType() == NotificationType.PORTFOLIO_MAX_LOSS) {
			return String.format("%s이(가) 최대 손실율에 도달했습니다", name);
		}
		throw new IllegalStateException("잘못된 타입입니다. type=" + getType());
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Long getIdToSentHistory() {
		return portfolioId;
	}
}
