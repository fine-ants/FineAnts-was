package codesquad.fineants.domain.notification.domain.entity.policy.target_gain;

import codesquad.fineants.domain.notification.domain.entity.policy.NotificationCondition;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TargetGainActiveCondition implements NotificationCondition<Portfolio> {
	@Override
	public boolean isSatisfiedBy(Portfolio portfolio) {
		boolean result = portfolio.isSameTargetGainActive(true);
		log.debug("TargetGainActiveCondition : {}", result);
		return result;
	}
}
