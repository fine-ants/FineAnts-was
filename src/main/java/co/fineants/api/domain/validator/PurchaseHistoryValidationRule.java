package co.fineants.api.domain.validator;

import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;

public interface PurchaseHistoryValidationRule {
	void validate(PurchaseHistory purchaseHistory);
}
