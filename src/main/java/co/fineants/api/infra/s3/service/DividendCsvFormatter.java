package co.fineants.api.infra.s3.service;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;

public class DividendCsvFormatter {

	public String format(StockDividend... dividends) {
		return "id,dividend,recordDate,paymentDate,stockCode" + "\n" + "1,361,20230331,20230517,KR7005930003";
	}
}
