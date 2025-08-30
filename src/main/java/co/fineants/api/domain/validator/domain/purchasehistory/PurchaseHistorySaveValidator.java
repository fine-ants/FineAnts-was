package co.fineants.api.domain.validator.domain.purchasehistory;

import java.util.Arrays;
import java.util.List;

import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.validator.domain.PurchaseHistoryValidationRule;
import co.fineants.api.domain.validator.domain.Validator;

public class PurchaseHistorySaveValidator implements Validator<PurchaseHistory> {

	private final List<PurchaseHistoryValidationRule> rules;

	public PurchaseHistorySaveValidator(PurchaseHistoryValidationRule... rules) {
		this.rules = Arrays.asList(rules);
	}

	@Override
	public void validate(PurchaseHistory target) {
		for (PurchaseHistoryValidationRule rule : rules) {
			rule.validate(target);
		}
	}
}
