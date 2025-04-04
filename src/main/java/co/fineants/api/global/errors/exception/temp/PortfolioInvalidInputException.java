package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class PortfolioInvalidInputException extends InvalidInputException {
	public PortfolioInvalidInputException(String portfolio, Throwable throwable) {
		super(portfolio, ErrorCode.PORTFOLIO_BAD_REQUEST, throwable);
	}
}
