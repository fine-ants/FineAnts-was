package codesquad.fineants.domain.portfolio.service;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.portfolio.domain.dto.request.PortfolioNotificationUpdateRequest;
import codesquad.fineants.domain.portfolio.domain.dto.response.PortfolioNotificationUpdateResponse;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.repository.PortfolioRepository;
import codesquad.fineants.global.common.authorized.Authorized;
import codesquad.fineants.global.common.authorized.service.PortfolioAuthorizedService;
import codesquad.fineants.global.common.resource.ResourceId;
import codesquad.fineants.global.errors.errorcode.PortfolioErrorCode;
import codesquad.fineants.global.errors.exception.NotFoundResourceException;
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
	public PortfolioNotificationUpdateResponse updateNotificationTargetGain(PortfolioNotificationUpdateRequest request,
		@ResourceId Long portfolioId) {
		log.info("포트폴리오 목표수익금액 알림 수정 서비스, request={}, portfolioId={}", request, portfolioId);
		Portfolio portfolio = findPortfolio(portfolioId);
		portfolio.changeTargetGainNotification(request.getIsActive());
		log.info("포트폴리오 목표수익금액 알림 수정 서비스 결과 : portfolio={}", portfolio);
		return PortfolioNotificationUpdateResponse.targetGainIsActive(portfolio);
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
