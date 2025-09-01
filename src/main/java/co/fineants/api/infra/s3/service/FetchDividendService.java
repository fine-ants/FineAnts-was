package co.fineants.api.infra.s3.service;

import java.util.List;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;

public interface FetchDividendService {
	List<StockDividend> fetchDividend();
}
