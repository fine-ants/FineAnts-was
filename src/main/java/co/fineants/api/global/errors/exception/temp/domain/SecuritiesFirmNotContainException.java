package co.fineants.api.global.errors.exception.temp.domain;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class SecuritiesFirmNotContainException extends DomainException {
	private final String securitiesFirm;

	public SecuritiesFirmNotContainException(String securitiesFirm) {
		super(String.format("Unlisted securitiesFirm: %s", securitiesFirm),
			CustomErrorCode.SECURITIES_FIRM_NOT_CONTAIN);
		this.securitiesFirm = securitiesFirm;
	}

	@Override
	public String toString() {
		return String.format("SecuritiesFirmNotContainException(securitiesFirm=%s, %s)", securitiesFirm,
			super.toString());
	}
}
