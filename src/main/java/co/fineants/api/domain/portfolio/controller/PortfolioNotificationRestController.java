package co.fineants.api.domain.portfolio.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.fineants.api.domain.portfolio.domain.dto.request.PortfolioNotificationUpdateRequest;
import co.fineants.api.domain.portfolio.domain.dto.response.PortfolioNotificationUpdateResponse;
import co.fineants.api.domain.portfolio.service.PortfolioNotificationService;
import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.success.PortfolioSuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/portfolio/{portfolioId}/notification")
@RestController
public class PortfolioNotificationRestController {

	private final PortfolioNotificationService service;

	@PutMapping("/targetGain")
	public ApiResponse<Void> updateNotificationTargetGain(
		@PathVariable Long portfolioId,
		@Valid @RequestBody PortfolioNotificationUpdateRequest request) {
		log.info("request={}, portfolioId={}", request, portfolioId);
		PortfolioNotificationUpdateResponse response = service.updateNotificationTargetGain(request.getIsActive(),
			portfolioId);
		if (Boolean.TRUE.equals(response.getIsActive())) {
			return ApiResponse.success(PortfolioSuccessCode.OK_MODIFY_PORTFOLIO_TARGET_GAIN_ACTIVE_NOTIFICATION);
		}
		return ApiResponse.success(PortfolioSuccessCode.OK_MODIFY_PORTFOLIO_TARGET_GAIN_INACTIVE_NOTIFICATION);
	}

	@PutMapping("/maxLoss")
	public ApiResponse<Void> updateNotificationMaximumLoss(
		@PathVariable Long portfolioId,
		@Valid @RequestBody PortfolioNotificationUpdateRequest request) {
		log.info("포트폴리오 알림 설정 : request={}, portfolioId={}", request, portfolioId);
		PortfolioNotificationUpdateResponse response = service.updateNotificationMaximumLoss(request.getIsActive(),
			portfolioId);
		if (Boolean.TRUE.equals(response.getIsActive())) {
			return ApiResponse.success(PortfolioSuccessCode.OK_MODIFY_PORTFOLIO_MAXIMUM_LOSS_ACTIVE_NOTIFICATION);
		}
		return ApiResponse.success(PortfolioSuccessCode.OK_MODIFY_PORTFOLIO_MAXIMUM_LOSS_INACTIVE_NOTIFICATION);
	}
}
