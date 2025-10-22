package co.fineants.stock.presentation;

import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.success.StockSuccessCode;
import co.fineants.api.infra.s3.service.WriteStockService;
import co.fineants.stock.application.FindStock;
import co.fineants.stock.application.SearchStock;
import co.fineants.stock.application.StockService;
import co.fineants.stock.presentation.dto.request.StockSearchRequest;
import co.fineants.stock.presentation.dto.response.StockReloadResponse;
import co.fineants.stock.presentation.dto.response.StockResponse;
import co.fineants.stock.presentation.dto.response.StockSearchItem;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;

@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@RestController
public class StockRestController {

	private final StockService stockService;
	private final SearchStock searchStock;
	private final WriteStockService writeStockService;
	private final FindStock findStock;

	@PostMapping("/search")
	@PermitAll
	public ApiResponse<List<StockSearchItem>> search(@RequestBody final StockSearchRequest request) {
		List<StockSearchItem> items = searchStock.search(request.getSearchTerm());
		return ApiResponse.success(StockSuccessCode.OK_SEARCH_STOCKS, items);
	}

	@GetMapping("/search")
	@PermitAll
	public ApiResponse<List<StockSearchItem>> search(
		@RequestParam(name = "tickerSymbol", required = false) String tickerSymbol,
		@RequestParam(name = "size", required = false, defaultValue = "10") int size,
		@RequestParam(name = "keyword", required = false) String keyword) {
		List<StockSearchItem> items = searchStock.search(tickerSymbol, size, keyword);
		return ApiResponse.success(StockSuccessCode.OK_SEARCH_STOCKS, items);
	}

	@PostMapping("/refresh")
	@Secured(value = {"ROLE_ADMIN"})
	public ApiResponse<StockReloadResponse> refreshStocks() {
		StockReloadResponse response = stockService.reloadStocks();
		return ApiResponse.success(StockSuccessCode.OK_REFRESH_STOCKS, response);
	}

	@PostMapping("/write/csv")
	@Secured(value = {"ROLE_ADMIN"})
	public ApiResponse<Void> writeAllStocks() {
		writeStockService.writeStocks(findStock.findAll());
		return ApiResponse.success(StockSuccessCode.OK_WRITE_STOCKS_CSV_TO_BUCKET);
	}

	@PostMapping("/sync")
	@Secured(value = {"ROLE_ADMIN"})
	public ApiResponse<Void> syncAllStocksWithLatestData() {
		stockService.syncAllStocksWithLatestData();
		return ApiResponse.success(StockSuccessCode.OK_REFRESH_STOCKS);
	}

	@GetMapping("/{tickerSymbol}")
	@PermitAll
	public ApiResponse<StockResponse> getStock(@PathVariable String tickerSymbol) {
		StockResponse response = searchStock.findDetailedStock(tickerSymbol);
		return ApiResponse.success(StockSuccessCode.OK_SEARCH_DETAIL_STOCK, response);
	}
}
