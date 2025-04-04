package co.fineants.api.global.errors.exception.temp.domain;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.global.errors.errorcode.ErrorCode;

public class TargetGainLessThanBudgetException extends DomainException {
	private final Money targetGain;
	private final Money budget;

	public TargetGainLessThanBudgetException(Money targetGain, Money budget) {
		super(String.format("Target gain cannot be less than budget: %s < %s", targetGain, budget),
			ErrorCode.TARGET_GAIN_LESS_THAN_BUDGET);
		this.targetGain = targetGain;
		this.budget = budget;
	}

	@Override
	public String toString() {
		return String.format("TargetGainLessThanBudgetException(targetGain=%s, budget=%s, %s)", targetGain, budget,
			super.toString());
	}
}
