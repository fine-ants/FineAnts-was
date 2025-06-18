package co.fineants.api.domain.holding.domain.rule;

import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.global.errors.exception.business.CashNotSufficientInvalidInputException;

public class CashSufficientRule implements PurchaseHistoryValidationRule {

	private final Portfolio portfolio;
	private final PortfolioCalculator calculator;

	public CashSufficientRule(Portfolio portfolio, PortfolioCalculator calculator) {
		this.portfolio = portfolio;
		this.calculator = calculator;
	}

	@Override
	public void validate(PurchaseHistory purchaseHistory) {
		Expression purchasedAmount = purchaseHistory.getNumShares()
			.multiply(purchaseHistory.getPurchasePricePerShare());
		if (!portfolio.isCashSufficientForPurchase(purchasedAmount, calculator)) {
			throw new CashNotSufficientInvalidInputException(purchaseHistory.toString());
		}
	}
}
