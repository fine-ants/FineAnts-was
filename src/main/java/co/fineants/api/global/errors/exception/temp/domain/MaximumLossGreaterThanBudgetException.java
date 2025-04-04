package co.fineants.api.global.errors.exception.temp.domain;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.global.errors.errorcode.ErrorCode;

public class MaximumLossGreaterThanBudgetException extends DomainException {
	private final Money maximumLoss;
	private final Money budget;

	public MaximumLossGreaterThanBudgetException(Money maximumLoss, Money budget) {
		super(String.format("Maximum loss cannot be greater than budget: %s > %s", maximumLoss, budget),
			ErrorCode.MAXIMUM_LOSS_GREATER_THAN_BUDGET);
		this.maximumLoss = maximumLoss;
		this.budget = budget;
	}

	@Override
	public String toString() {
		return String.format("MaximumLossGreaterThanBudgetException(maximumLoss=%s, budget=%s, %s)", maximumLoss,
			budget, super.toString());
	}
}
