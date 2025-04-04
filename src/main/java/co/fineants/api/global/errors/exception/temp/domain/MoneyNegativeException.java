package co.fineants.api.global.errors.exception.temp.domain;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.global.errors.errorcode.ErrorCode;

public class MoneyNegativeException extends DomainException {

	private final Money money;

	public MoneyNegativeException(Money money) {
		super(String.format("Money cannot be negative: %s", money), ErrorCode.MONEY_NEGATIVE);
		this.money = money;
	}

	@Override
	public String toString() {
		return String.format("MoneyNegativeException(money=%s, %s)", money, super.toString());
	}
}
