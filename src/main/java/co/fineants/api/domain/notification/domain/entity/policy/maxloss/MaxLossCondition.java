package co.fineants.api.domain.notification.domain.entity.policy.maxloss;

import co.fineants.api.domain.notification.domain.entity.policy.NotificationCondition;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MaxLossCondition implements NotificationCondition<Portfolio> {

	@Override
	public boolean isSatisfiedBy(Portfolio portfolio) {
		boolean result = portfolio.reachedMaximumLoss();
		log.debug("MaxLossCondition.isSatisfieldBy : {}", result);
		return result;
	}
}