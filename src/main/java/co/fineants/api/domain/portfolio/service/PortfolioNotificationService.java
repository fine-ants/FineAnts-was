package co.fineants.api.domain.portfolio.service;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.portfolio.domain.dto.request.PortfolioNotificationUpdateRequest;
import co.fineants.api.domain.portfolio.domain.dto.response.PortfolioNotificationUpdateResponse;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.global.common.authorized.Authorized;
import co.fineants.api.global.common.authorized.service.PortfolioAuthorizedService;
import co.fineants.api.global.common.resource.ResourceId;
import co.fineants.api.global.errors.errorcode.PortfolioErrorCode;
import co.fineants.api.global.errors.exception.BadRequestException;
import co.fineants.api.global.errors.exception.NotFoundResourceException;
import co.fineants.api.global.errors.exception.portfolio.IllegalPortfolioStateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PortfolioNotificationService {

	private final PortfolioRepository portfolioRepository;

	@Transactional
	@Authorized(serviceClass = PortfolioAuthorizedService.class)
	@Secured("ROLE_USER")
	public PortfolioNotificationUpdateResponse updateNotificationTargetGain(boolean active,
		@ResourceId Long portfolioId) {
		log.info("change the Portfolio's targetGainIsActive, active={}, portfolioId={}", active, portfolioId);
		Portfolio portfolio = findPortfolio(portfolioId);
		changeTargetGainNotification(portfolio, active);
		return PortfolioNotificationUpdateResponse.targetGainIsActive(portfolioId, active);
	}

	private void changeTargetGainNotification(Portfolio portfolio, boolean isActive) {
		try {
			portfolio.changeTargetGainNotification(isActive);
		} catch (IllegalPortfolioStateException e) {
			throw new BadRequestException(e.getErrorCode(), e);
		}
	}

	private Portfolio findPortfolio(Long portfolioId) {
		return portfolioRepository.findById(portfolioId)
			.orElseThrow(() -> new NotFoundResourceException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
	}

	@Transactional
	@Authorized(serviceClass = PortfolioAuthorizedService.class)
	@Secured("ROLE_USER")
	public PortfolioNotificationUpdateResponse updateNotificationMaximumLoss(PortfolioNotificationUpdateRequest request,
		@ResourceId Long portfolioId) {
		log.info("포트폴리오 최대손실금액 알림 수정 서비스, request={}, portfolioId={}", request, portfolioId);
		Portfolio portfolio = findPortfolio(portfolioId);
		portfolio.changeMaximumLossNotification(request.getIsActive());
		log.info("포트폴리오 최대손실금액 알림 수정 서비스 결과 : portfolio={}", portfolio);
		return PortfolioNotificationUpdateResponse.maximumLossIsActive(portfolio);
	}
}
