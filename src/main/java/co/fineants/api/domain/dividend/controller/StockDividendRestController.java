package co.fineants.api.domain.dividend.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.fineants.api.domain.dividend.service.StockDividendService;
import co.fineants.api.domain.stock.domain.entity.StockDividend;
import co.fineants.api.domain.stock.service.StockService;
import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.success.StockDividendSuccessCode;
import co.fineants.api.infra.s3.service.WriteDividendService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dividends")
@RequiredArgsConstructor
public class StockDividendRestController {

	private final StockDividendService service;
	private final WriteDividendService writeDividendService;
	private final StockService stockService;

	@PostMapping("/init")
	@Secured("ROLE_ADMIN")
	public ApiResponse<Void> initializeStockDividend() {
		service.initializeStockDividend();
		return ApiResponse.success(StockDividendSuccessCode.OK_INIT_DIVIDENDS);
	}

	@PostMapping("/refresh")
	@Secured("ROLE_ADMIN")
	public ApiResponse<Void> refreshStockDividend() {
		service.reloadStockDividend();
		return ApiResponse.success(StockDividendSuccessCode.OK_REFRESH_DIVIDENDS);
	}

	@PostMapping("/write/csv")
	@Secured("ROLE_ADMIN")
	public ApiResponse<Void> writeDividendCsvToBucket() {
		StockDividend[] stockDividends = stockService.getAllStockDividends().toArray(StockDividend[]::new);
		writeDividendService.writeDividend(stockDividends);
		return ApiResponse.success(StockDividendSuccessCode.OK_WRITE_DIVIDENDS_CSV);
	}
}
