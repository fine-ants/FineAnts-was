package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class PortfolioNotFoundException extends NotFoundException {
	public PortfolioNotFoundException(String value) {
		super(value, ErrorCode.PORTFOLIO_NOT_FOUND);
	}
}
