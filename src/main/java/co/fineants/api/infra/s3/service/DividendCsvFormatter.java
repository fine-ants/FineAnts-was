package co.fineants.api.infra.s3.service;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;

public class DividendCsvFormatter {

	public String format(StockDividend... dividends) {
		return "id,dividend,recordDate,paymentDate,stockCode";
	}
}
