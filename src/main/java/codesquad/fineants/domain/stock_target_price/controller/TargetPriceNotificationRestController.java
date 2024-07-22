package codesquad.fineants.domain.stock_target_price.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.stock_target_price.domain.dto.request.TargetPriceNotificationDeleteRequest;
import codesquad.fineants.domain.stock_target_price.domain.dto.response.TargetPriceNotificationDeleteResponse;
import codesquad.fineants.domain.stock_target_price.service.TargetPriceNotificationService;
import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.security.oauth.dto.MemberAuthentication;
import codesquad.fineants.global.security.oauth.resolver.MemberAuthenticationPrincipal;
import codesquad.fineants.global.success.StockSuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TargetPriceNotificationRestController {

	private final TargetPriceNotificationService service;

	// 종목 지정가 알림 전체 삭제
	@DeleteMapping("/api/stocks/target-price/notifications")
	public ApiResponse<Void> deleteAllStockTargetPriceNotification(
		@Valid @RequestBody TargetPriceNotificationDeleteRequest request,
		@MemberAuthenticationPrincipal MemberAuthentication authentication) {
		TargetPriceNotificationDeleteResponse response = service.deleteAllStockTargetPriceNotification(
			request.getTargetPriceNotificationIds(),
			request.getTickerSymbol(),
			authentication.getId());
		log.info("종목 지정가 알림 제거 결과 : {}", response);
		return ApiResponse.success(StockSuccessCode.OK_DELETE_TARGET_PRICE_NOTIFICATIONS);
	}

	// 종목 지정가 알림 단일 삭제
	@DeleteMapping("/api/stocks/target-price/notifications/{targetPriceNotificationId}")
	public ApiResponse<Void> deleteStockTargetPriceNotification(
		@PathVariable Long targetPriceNotificationId
	) {
		TargetPriceNotificationDeleteResponse response = service.deleteStockTargetPriceNotification(
			targetPriceNotificationId);
		log.info("종목 지정가 알림 제거 결과 : {}", response);
		return ApiResponse.success(StockSuccessCode.OK_DELETE_TARGET_PRICE_NOTIFICATIONS);
	}
}
