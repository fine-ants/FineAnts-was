package co.fineants.api.domain.holding.domain.rule;

import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;

public interface PurchaseHistoryValidationRule {
	void validate(PurchaseHistory purchaseHistory);
}
