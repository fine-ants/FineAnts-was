package co.fineants.api.global.errors.exception.domain;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class PortfolioNameInvalidException extends DomainException {
	private final String name;

	public PortfolioNameInvalidException(String name) {
		super(String.format("Invalid Portfolio name: %s", name), ErrorCode.PORTFOLIO_NAME_INVALID);
		this.name = name;
	}

	@Override
	public String toString() {
		return String.format("PortfolioNameInvalidException(name=%s, %s)", name, super.toString());
	}
}
