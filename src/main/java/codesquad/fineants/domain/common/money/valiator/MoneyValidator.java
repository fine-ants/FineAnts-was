package codesquad.fineants.domain.common.money.valiator;

import java.math.BigDecimal;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import codesquad.fineants.domain.common.money.Money;

public class MoneyValidator implements ConstraintValidator<MoneyNumber, Money> {
	@Override
	public boolean isValid(codesquad.fineants.domain.common.money.Money value, ConstraintValidatorContext context) {
		if (value == null) {
			return false;
		}
		return value.getAmount().compareTo(BigDecimal.ZERO) >= 0;
	}
}