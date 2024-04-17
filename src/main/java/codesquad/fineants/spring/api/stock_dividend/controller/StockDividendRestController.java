package codesquad.fineants.spring.api.stock_dividend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.spring.api.common.response.ApiResponse;
import codesquad.fineants.spring.api.common.success.StockDividendSuccessCode;
import codesquad.fineants.spring.api.stock_dividend.service.StockDividendService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dividends")
public class StockDividendRestController {

	private final StockDividendService service;

	@PostMapping("/init")
	public ApiResponse<Void> initStockDividend() {
		service.initStockDividend();
		return ApiResponse.success(StockDividendSuccessCode.OK_INIT_DIVIDENDS);
	}
}
