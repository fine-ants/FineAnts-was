package co.fineants.api.domain.common.money;

import java.math.BigDecimal;
import java.util.Objects;

import co.fineants.api.domain.common.count.Count;
import jakarta.annotation.Nonnull;

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
	public Expression minus(Expression subtrahend) {
		return new Subtraction(this, subtrahend);
	}

	@Override
	public Expression times(int multiplier) {
		return new Sum(augend.times(multiplier), addend.times(multiplier));
	}

	@Override
	public Expression divide(Count divisor) {
		return new AverageDivision(this, divisor);
	}

	@Override
	public RateDivision divide(Expression divisor) {
		return new RateDivision(this, divisor);
	}

	@Override
	public Percentage toPercentage(Bank bank, Currency to) {
		return Percentage.from(reduce(bank, to).amount);
	}

	@Override
	public int compareTo(@Nonnull Expression expression) {
		Objects.requireNonNull(expression, "Comparison target must not be null");
		Bank bank = Bank.getInstance();
		Money won1 = bank.reduce(this, Currency.KRW);
		Money won2 = bank.reduce(expression, Currency.KRW);
		return won1.compareTo(won2);
	}

	@Override
	public String toString() {
		return String.format("%s+%s", augend, addend);
	}
}
