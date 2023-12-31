package codesquad.fineants.spring.api.watch_list;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalMember;
import codesquad.fineants.spring.api.response.ApiResponse;
import codesquad.fineants.spring.api.success.code.WatchListSuccessCode;
import codesquad.fineants.spring.api.watch_list.request.CreateWatchListRequest;
import codesquad.fineants.spring.api.watch_list.request.CreateWatchStockRequest;
import codesquad.fineants.spring.api.watch_list.response.CreateWatchListResponse;
import codesquad.fineants.spring.api.watch_list.response.ReadWatchListResponse;
import codesquad.fineants.spring.api.watch_list.response.ReadWatchListsResponse;
import lombok.RequiredArgsConstructor;


@RequestMapping("/api/watchlists")
@RequiredArgsConstructor
@RestController
public class WatchListRestController {

	private final WatchListService watchListService;

	@PostMapping
	public ApiResponse<CreateWatchListResponse> createWatchList(@AuthPrincipalMember AuthMember authMember,
		@RequestBody CreateWatchListRequest request){
		return ApiResponse.success(WatchListSuccessCode.CREATED_WATCH_LIST,
			watchListService.createWatchList(authMember, request));
	}

	@GetMapping
	public ApiResponse<List<ReadWatchListsResponse>> readWatchLists(@AuthPrincipalMember AuthMember authMember){
		return ApiResponse.success(WatchListSuccessCode.READ_WATCH_LISTS, watchListService.readWatchLists(authMember));
	}

	@GetMapping("/{watchlistId}")
	public ApiResponse<List<ReadWatchListResponse>> readWatchList(@AuthPrincipalMember AuthMember authMember,
		@PathVariable Long watchlistId){
		return ApiResponse.success(WatchListSuccessCode.READ_WATCH_LIST,
			watchListService.readWatchList(authMember, watchlistId));
	}

	@DeleteMapping("/{watchlistId}")
	public ApiResponse<Void> deleteWatchList(@AuthPrincipalMember AuthMember authMember,
		@PathVariable Long watchlistId){
		watchListService.deleteWatchList(authMember, watchlistId);
		return ApiResponse.success(WatchListSuccessCode.DELETED_WATCH_LIST);
	}

	@PostMapping("/{watchlistId}/stock")
	public ApiResponse<Void> createWatchStock(@AuthPrincipalMember AuthMember authMember,
		@PathVariable Long watchlistId, @RequestBody CreateWatchStockRequest request){
		watchListService.createWatchStock(authMember, watchlistId, request);
		return ApiResponse.success(WatchListSuccessCode.CREATED_WATCH_STOCK);
	}

	@DeleteMapping("/{watchlistId}/stock/{stockId}")
	public ApiResponse<Void> deleteWatchStock(@AuthPrincipalMember AuthMember authMember,
		@PathVariable("watchlistId") Long watchListId, @PathVariable("stockId") Long stockId){
		watchListService.deleteWatchStock(authMember, watchListId, stockId);
		return ApiResponse.success(WatchListSuccessCode.DELETED_WATCH_STOCK);
	}
}
