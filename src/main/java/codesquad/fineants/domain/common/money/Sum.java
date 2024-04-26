package codesquad.fineants.domain.common.money;

import java.math.BigDecimal;

public class Sum implements Expression {
	private final Expression augend;
	private final Expression addend;

	public Sum(Expression augend, Expression addend) {
		this.augend = augend;
		this.addend = addend;
	}

	@Override
	public Money reduce(Bank bank, Currency to) {
		BigDecimal amount = bank.reduce(augend, to).amount.add(bank.reduce(addend, to).amount);
		return new Money(amount, to);
	}

	@Override
	public Expression plus(Expression addend) {
		return new Sum(this, addend);
	}

	@Override
	public Expression times(int multiplier) {
		return new Sum(augend.times(multiplier), addend.times(multiplier));
	}

}
