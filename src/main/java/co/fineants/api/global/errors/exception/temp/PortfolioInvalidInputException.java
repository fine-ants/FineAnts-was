package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class PortfolioInvalidInputException extends InvalidInputException {
	public PortfolioInvalidInputException(String portfolio, Throwable throwable) {
		super(portfolio, CustomErrorCode.PORTFOLIO_BAD_REQUEST, throwable);
	}
}
