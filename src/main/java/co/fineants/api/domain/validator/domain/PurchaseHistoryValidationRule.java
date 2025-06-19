package co.fineants.api.domain.validator.domain;

import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;

public interface PurchaseHistoryValidationRule {
	void validate(PurchaseHistory purchaseHistory);
}
