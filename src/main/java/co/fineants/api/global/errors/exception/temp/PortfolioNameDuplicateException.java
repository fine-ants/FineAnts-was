package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class PortfolioNameDuplicateException extends DuplicateException {
	public PortfolioNameDuplicateException(String portfolioName) {
		super(portfolioName, CustomErrorCode.PORTFOLIO_NAME_DUPLICATE);
	}
}
