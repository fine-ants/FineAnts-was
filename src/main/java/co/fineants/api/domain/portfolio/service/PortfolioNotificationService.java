package co.fineants.api.domain.portfolio.service;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.portfolio.domain.dto.response.PortfolioNotificationUpdateResponse;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.global.common.authorized.Authorized;
import co.fineants.api.global.common.authorized.service.PortfolioAuthorizedService;
import co.fineants.api.global.common.resource.ResourceId;
import co.fineants.api.global.errors.errorcode.PortfolioErrorCode;
import co.fineants.api.global.errors.exception.BadRequestException;
import co.fineants.api.global.errors.exception.NotFoundResourceException;
import co.fineants.api.global.errors.exception.portfolio.IllegalPortfolioFinancialStateException;
import co.fineants.api.global.errors.exception.portfolio.PortfolioUpdateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PortfolioNotificationService {

	private final PortfolioRepository repository;

	/**
	 * 포트폴리오의 목표수익금액 활성화 알림 설정을 변경 후 결과를 반환.
	 *
	 * @param active 알림 활성화 여부, true: 알림 활성화, false: 알림 비활성화
	 * @param portfolioId 포트폴리오 식별 번호
	 * @return 포트폴리오 목표수익금액 활성화 알림 변경 결과
	 * @throws PortfolioUpdateException 포트폴리오 수정에 실패하면 예외가 발생함
	 */
	@Transactional
	@Authorized(serviceClass = PortfolioAuthorizedService.class)
	@Secured("ROLE_USER")
	public PortfolioNotificationUpdateResponse updateNotificationTargetGain(Boolean active,
		@ResourceId Long portfolioId) throws PortfolioUpdateException {
		Portfolio portfolio = findPortfolio(portfolioId);
		try {
			portfolio.changeTargetGainNotification(active);
		} catch (IllegalPortfolioFinancialStateException e) {
			String message = "can't change the targetGainNotification active status";
			throw new PortfolioUpdateException(message, e);
		}
		return PortfolioNotificationUpdateResponse.targetGainIsActive(portfolioId, active);
	}

	private Portfolio findPortfolio(Long portfolioId) {
		return repository.findById(portfolioId)
			.orElseThrow(() -> new NotFoundResourceException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
	}

	/**
	 * 포트폴리오 최대손실금액의 알림 활성화를 설정 후 결과를 반환.
	 *
	 * @param active 알림 활성화 여부, true: 알림 활성화, false: 알림 비활성화
	 * @param portfolioId 포트폴리오 식별 번호
	 * @return 포트폴리오 최대손실금액 알림 활성화 설정 변경 결과
	 * @throws BadRequestException 포트폴리오의 최대손실금액이 0원인 경우 예외 발생
	 */
	@Transactional
	@Authorized(serviceClass = PortfolioAuthorizedService.class)
	@Secured("ROLE_USER")
	public PortfolioNotificationUpdateResponse updateNotificationMaximumLoss(Boolean active,
		@ResourceId Long portfolioId) {
		Portfolio portfolio = findPortfolio(portfolioId);
		try {
			portfolio.changeMaximumLossNotification(active);
		} catch (IllegalPortfolioFinancialStateException e) {
			String message = "can't change the maximumLossNotification active status";
			throw new PortfolioUpdateException(message, e);
		}
		return PortfolioNotificationUpdateResponse.maximumLossIsActive(portfolio);
	}
}
