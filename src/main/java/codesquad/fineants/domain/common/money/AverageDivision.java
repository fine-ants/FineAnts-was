package codesquad.fineants.domain.common.money;

import static codesquad.fineants.domain.common.money.Currency.*;

import org.jetbrains.annotations.NotNull;

import codesquad.fineants.domain.common.count.Count;

public class AverageDivision implements Expression {

	private final Expression division;
	private final Count divisor;

	public AverageDivision(Expression division, Count divisor) {
		this.division = division;
		this.divisor = divisor;
	}

	@Override
	public Money reduce(Bank bank, Currency to) {
		Expression result = divisor.division(bank.reduce(division, to));
		return bank.reduce(result, to);
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
		return new AverageDivision(division.times(multiplier), divisor);
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
	public int compareTo(@NotNull Expression o) {
		Bank bank = Bank.getInstance();
		Currency to = KRW;
		Money won = this.reduce(bank, to);
		Money won2 = o.reduce(bank, to);
		return won.compareTo(won2);
	}
}
