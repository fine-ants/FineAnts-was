package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class PortfolioNotFoundException extends NotFoundException {
	public PortfolioNotFoundException(String value) {
		super(value, CustomErrorCode.PORTFOLIO_NOT_FOUND);
	}
}
