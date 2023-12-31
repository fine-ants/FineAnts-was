package codesquad.fineants.spring.api.portfolio_notification;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalMember;
import codesquad.fineants.spring.api.portfolio_notification.request.PortfolioNotificationModifyRequest;
import codesquad.fineants.spring.api.portfolio_notification.response.PortfolioNotificationModifyResponse;
import codesquad.fineants.spring.api.response.ApiResponse;
import codesquad.fineants.spring.api.success.code.PortfolioSuccessCode;
import codesquad.fineants.spring.auth.HasPortfolioAuthorization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/portfolio/{portfolioId}/notification")
@RestController
public class PortfolioNotificationRestController {

	private final PortfolioNotificationService service;

	@HasPortfolioAuthorization
	@PutMapping("/targetGain")
	public ApiResponse<Void> modifyNotificationTargetGain(
		@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember,
		@Valid @RequestBody PortfolioNotificationModifyRequest request) {
		log.info("포트폴리오 알림 설정 : request={}, portfolioId={}", request, portfolioId);
		PortfolioNotificationModifyResponse response = service.modifyPortfolioTargetGainNotification(request,
			portfolioId);
		if (response.getIsActive()) {
			return ApiResponse.success(PortfolioSuccessCode.OK_MODIFY_PORTFOLIO_TARGET_GAIN_ACTIVE_NOTIFICATION);
		}
		return ApiResponse.success(PortfolioSuccessCode.OK_MODIFY_PORTFOLIO_TARGET_GAIN_INACTIVE_NOTIFICATION);
	}

	@HasPortfolioAuthorization
	@PutMapping("/maxLoss")
	public ApiResponse<Void> modifyNotificationMaximumLoss(
		@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember,
		@Valid @RequestBody PortfolioNotificationModifyRequest request) {
		log.info("포트폴리오 알림 설정 : request={}, portfolioId={}", request, portfolioId);
		PortfolioNotificationModifyResponse response = service.modifyPortfolioMaximumLossNotification(request,
			portfolioId);
		if (response.getIsActive()) {
			return ApiResponse.success(PortfolioSuccessCode.OK_MODIFY_PORTFOLIO_MAXIMUM_LOSS_ACTIVE_NOTIFICATION);
		}
		return ApiResponse.success(PortfolioSuccessCode.OK_MODIFY_PORTFOLIO_MAXIMUM_LOSS_INACTIVE_NOTIFICATION);
	}
}
