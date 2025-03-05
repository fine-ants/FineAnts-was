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
import co.fineants.api.global.errors.exception.BadRequestException;
import co.fineants.api.global.errors.exception.portfolio.PortfolioUpdateException;
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

	/**
	 * 포트폴리오의 최대손실율 알림 활성화 상태를 수정한다.
	 *
	 * @param portfolioId 포트폴리오 식별자 아이디
	 * @param request 변경하고자 하는 활성화 상태 정보
	 * @return 포트폴리오 알림 활성화 변경 완료 메시지 응답
	 * @throws BadRequestException 포트폴리오의 최대손실율 알림 활성화 상태를 수정하지 못하면 예외가 발생함
	 */
	@PutMapping("/maxLoss")
	public ApiResponse<Void> updateNotificationMaximumLoss(
		@PathVariable Long portfolioId,
		@Valid @RequestBody PortfolioNotificationUpdateRequest request) throws BadRequestException {
		PortfolioNotificationUpdateResponse response;
		try {
			response = service.updateNotificationMaximumLoss(request.getIsActive(),
				portfolioId);
		} catch (PortfolioUpdateException exception) {
			String message = "can't update the NotificationMaximumLoss";
			throw new BadRequestException(null, message, exception);
		}

		if (Boolean.TRUE.equals(response.getIsActive())) {
			return ApiResponse.success(PortfolioSuccessCode.OK_MODIFY_PORTFOLIO_MAXIMUM_LOSS_ACTIVE_NOTIFICATION);
		}
		return ApiResponse.success(PortfolioSuccessCode.OK_MODIFY_PORTFOLIO_MAXIMUM_LOSS_INACTIVE_NOTIFICATION);
	}
}
