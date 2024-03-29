package codesquad.fineants.spring.api.kis.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.spring.api.common.response.ApiResponse;
import codesquad.fineants.spring.api.common.success.KisSuccessCode;
import codesquad.fineants.spring.api.kis.client.KisCurrentPrice;
import codesquad.fineants.spring.api.kis.request.StockPriceRefreshRequest;
import codesquad.fineants.spring.api.kis.response.KisClosingPrice;
import codesquad.fineants.spring.api.kis.service.KisService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/kis")
@RequiredArgsConstructor
public class KisRestController {

	private final KisService service;

	// 종목 현재가 갱신
	@PostMapping("/current-price/all/refresh")
	public ApiResponse<List<KisCurrentPrice>> refreshAllStockCurrentPrice() {
		List<KisCurrentPrice> responses = service.refreshAllStockCurrentPrice();
		return ApiResponse.success(KisSuccessCode.OK_REFRESH_CURRENT_PRICE_STOCKS, responses);
	}

	// 특정 종목 현재가 갱신
	@PostMapping("/current-price/refresh")
	public ApiResponse<List<KisCurrentPrice>> refreshStockCurrentPrice(
		@RequestBody StockPriceRefreshRequest request
	) {
		List<KisCurrentPrice> response = service.refreshStockCurrentPrice(request.getTickerSymbols());
		return ApiResponse.success(KisSuccessCode.OK_REFRESH_CURRENT_PRICE_STOCKS, response);
	}

	// 한 종목 현재가 조회
	@GetMapping("/current-price/{tickerSymbol}")
	public Mono<ApiResponse<KisCurrentPrice>> fetchCurrentPrice(
		@PathVariable String tickerSymbol
	) {
		return service.fetchCurrentPrice(tickerSymbol)
			.map(currentPrice -> ApiResponse.success(KisSuccessCode.OK_FETCH_CURRENT_PRICE, currentPrice));
	}

	// 모든 종목 종가 갱신
	@PostMapping("/closing-price/all/refresh")
	public ApiResponse<List<KisClosingPrice>> refreshAllLastDayClosingPrice() {
		List<KisClosingPrice> responses = service.refreshAllLastDayClosingPrice();
		return ApiResponse.success(KisSuccessCode.OK_REFRESH_LAST_DAY_CLOSING_PRICE, responses);
	}

	// 특정 종목 종가 갱신
	@PostMapping("/closing-price/refresh")
	public ApiResponse<List<KisClosingPrice>> refreshLastDayClosingPrice(
		@RequestBody StockPriceRefreshRequest request
	) {
		List<KisClosingPrice> responses = service.refreshLastDayClosingPrice(request.getTickerSymbols());
		return ApiResponse.success(KisSuccessCode.OK_REFRESH_LAST_DAY_CLOSING_PRICE, responses);
	}
}
