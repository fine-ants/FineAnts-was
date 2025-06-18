package co.fineants.api.domain.validator.purchasehistory;

import java.util.Arrays;
import java.util.List;

import co.fineants.api.domain.validator.PurchaseHistoryValidationRule;
import co.fineants.api.domain.validator.Validator;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;

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
