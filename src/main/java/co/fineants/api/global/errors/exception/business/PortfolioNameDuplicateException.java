package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class PortfolioNameDuplicateException extends DuplicateException {
	public PortfolioNameDuplicateException(String portfolioName) {
		super(portfolioName, ErrorCode.PORTFOLIO_NAME_DUPLICATE);
	}
}
