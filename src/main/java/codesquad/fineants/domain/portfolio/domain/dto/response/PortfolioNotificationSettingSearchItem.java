package codesquad.fineants.domain.portfolio.domain.dto.response;

import java.time.LocalDateTime;

import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
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
public class PortfolioNotificationSettingSearchItem {
	private Long portfolioId;
	private String securitiesFirm;
	private String name;
	private Boolean targetGainNotify;
	private Boolean maxLossNotify;
	private Boolean isTargetGainSet;
	private Boolean isMaxLossSet;
	private LocalDateTime createdAt;

	public static PortfolioNotificationSettingSearchItem from(Portfolio portfolio) {
		return PortfolioNotificationSettingSearchItem.builder()
			.portfolioId(portfolio.getId())
			.securitiesFirm(portfolio.getSecuritiesFirm())
			.name(portfolio.getName())
			.targetGainNotify(portfolio.getTargetGainIsActive())
			.maxLossNotify(portfolio.getMaximumLossIsActive())
			.isTargetGainSet(portfolio.isTargetGainSet())
			.isMaxLossSet(portfolio.isMaximumLossSet())
			.createdAt(portfolio.getCreateAt())
			.build();
	}
}
