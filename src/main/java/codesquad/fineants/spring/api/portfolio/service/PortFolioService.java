package codesquad.fineants.spring.api.portfolio.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistoryRepository;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.portfolio_holding.PortfolioHoldingRepository;
import codesquad.fineants.domain.purchase_history.PurchaseHistoryRepository;
import codesquad.fineants.spring.api.common.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.common.errors.errorcode.PortfolioErrorCode;
import codesquad.fineants.spring.api.common.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.common.errors.exception.ConflictException;
import codesquad.fineants.spring.api.common.errors.exception.ForBiddenException;
import codesquad.fineants.spring.api.common.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.portfolio.manager.PortfolioPropertiesManager;
import codesquad.fineants.spring.api.portfolio.request.PortfolioCreateRequest;
import codesquad.fineants.spring.api.portfolio.request.PortfolioModifyRequest;
import codesquad.fineants.spring.api.portfolio.request.PortfoliosDeleteRequest;
import codesquad.fineants.spring.api.portfolio.response.PortFolioCreateResponse;
import codesquad.fineants.spring.api.portfolio.response.PortfolioModifyResponse;
import codesquad.fineants.spring.api.portfolio.response.PortfoliosResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PortFolioService {

	private final PortfolioRepository portfolioRepository;
	private final MemberRepository memberRepository;
	private final PortfolioHoldingRepository portfolioHoldingRepository;
	private final PurchaseHistoryRepository purchaseHistoryRepository;
	private final PortfolioGainHistoryRepository portfolioGainHistoryRepository;
	private final CurrentPriceManager currentPriceManager;
	private final PortfolioPropertiesManager portfolioPropertiesManager;

	@Transactional
	public PortFolioCreateResponse createPortfolio(PortfolioCreateRequest request, AuthMember authMember) {
		validateTargetGainIsEqualLessThanBudget(request.getTargetGain(), request.getBudget());
		validateMaximumLossIsEqualGraterThanBudget(request.getMaximumLoss(), request.getBudget());
		validateSecuritiesFirm(request.getSecuritiesFirm());

		Member member = findMember(authMember.getMemberId());

		validateUniquePortfolioName(request.getName(), member);
		Portfolio portfolio = request.toEntity(member);
		return PortFolioCreateResponse.from(portfolioRepository.save(portfolio));
	}

	private Member findMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new NotFoundResourceException(MemberErrorCode.NOT_FOUND_MEMBER));
	}

	private void validateTargetGainIsEqualLessThanBudget(Long targetGain, Long budget) {
		if (budget > 0 && targetGain <= budget) {
			throw new BadRequestException(PortfolioErrorCode.TARGET_GAIN_LOSS_IS_EQUAL_LESS_THAN_BUDGET);
		}
	}

	private void validateMaximumLossIsEqualGraterThanBudget(Long maximumLoss, Long budget) {
		if (budget > 0 && maximumLoss >= budget) {
			throw new BadRequestException(PortfolioErrorCode.MAXIMUM_LOSS_IS_EQUAL_GREATER_THAN_BUDGET);
		}
	}

	private void validateSecuritiesFirm(String securitiesFirm) {
		if (!portfolioPropertiesManager.contains(securitiesFirm)) {
			throw new BadRequestException(PortfolioErrorCode.SECURITIES_FIRM_IS_NOT_CONTAINS);
		}
	}

	private void validateUniquePortfolioName(String name, Member member) {
		if (portfolioRepository.existsByNameAndMember(name, member)) {
			throw new ConflictException(PortfolioErrorCode.DUPLICATE_NAME);
		}
	}

	@Transactional
	public PortfolioModifyResponse updatePortfolio(PortfolioModifyRequest request, Long portfolioId,
		AuthMember authMember) {
		log.info("포트폴리오 수정 서비스 요청 : request={}, portfolioId={}, authMember={}", request, portfolioId, authMember);

		validateTargetGainIsEqualLessThanBudget(request.getTargetGain(), request.getBudget());
		validateMaximumLossIsEqualGraterThanBudget(request.getMaximumLoss(), request.getBudget());

		Member member = findMember(authMember.getMemberId());
		Portfolio originalPortfolio = findPortfolio(portfolioId);
		Portfolio changePortfolio = request.toEntity(member);

		validatePortfolioAuthorization(originalPortfolio, authMember.getMemberId());
		if (!originalPortfolio.isSameName(changePortfolio)) {
			validateUniquePortfolioName(changePortfolio.getName(), member);
		}
		originalPortfolio.change(changePortfolio);

		log.info("변경된 포트폴리오 결과 : {}", originalPortfolio);
		return PortfolioModifyResponse.from(changePortfolio);
	}

	private void validatePortfolioAuthorization(Portfolio portfolio, Long memberId) {
		if (!portfolio.hasAuthorization(memberId)) {
			throw new ForBiddenException(PortfolioErrorCode.NOT_HAVE_AUTHORIZATION);
		}
	}

	@Transactional
	public void deletePortfolio(Long portfolioId, AuthMember authMember) {
		log.info("포트폴리오 삭제 서비스 요청 : portfolioId={}, authMember={}", portfolioId, authMember);

		Portfolio findPortfolio = findPortfolio(portfolioId);
		validatePortfolioAuthorization(findPortfolio, authMember.getMemberId());

		List<Long> portfolioHoldingIds = portfolioHoldingRepository.findAllByPortfolio(findPortfolio).stream()
			.map(PortfolioHolding::getId)
			.collect(Collectors.toList());

		int delPortfolioGainHistoryCnt = portfolioGainHistoryRepository.deleteAllByPortfolioId(portfolioId);
		log.info("포트폴리오 손익 내역 삭제 개수 : {}", delPortfolioGainHistoryCnt);

		int delTradeHistoryCnt = purchaseHistoryRepository.deleteAllByPortfolioHoldingIdIn(portfolioHoldingIds);
		log.info("매매이력 삭제 개수 : {}", delTradeHistoryCnt);

		int delPortfolioCnt = portfolioHoldingRepository.deleteAllByPortfolioId(findPortfolio.getId());
		log.info("포트폴리오 종목 삭제 개수 : {}", delPortfolioCnt);

		portfolioRepository.deleteById(findPortfolio.getId());
		log.info("포트폴리오 삭제 : delPortfolio={}", findPortfolio);
	}

	@Transactional
	public void deletePortfolios(PortfoliosDeleteRequest request, AuthMember authMember) {
		for (Long portfolioId : request.getPortfolioIds()) {
			Portfolio portfolio = findPortfolio(portfolioId);
			validatePortfolioAuthorization(portfolio, authMember.getMemberId());
		}

		for (Long portfolioId : request.getPortfolioIds()) {
			Portfolio portfolio = findPortfolio(portfolioId);
			List<Long> portfolioStockIds = portfolioHoldingRepository.findAllByPortfolio(portfolio).stream()
				.map(PortfolioHolding::getId)
				.collect(Collectors.toList());
			purchaseHistoryRepository.deleteAllByPortfolioHoldingIdIn(portfolioStockIds);
			portfolioHoldingRepository.deleteAllByPortfolioId(portfolio.getId());
			portfolioGainHistoryRepository.deleteAllByPortfolioId(portfolioId);
			portfolioRepository.deleteById(portfolio.getId());
		}
	}

	public Portfolio findPortfolio(Long portfolioId) {
		return portfolioRepository.findById(portfolioId)
			.orElseThrow(() -> new NotFoundResourceException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
	}

	public PortfoliosResponse readMyAllPortfolio(AuthMember authMember) {
		List<Portfolio> portfolios = portfolioRepository.findAllByMemberIdOrderByIdDesc(
			authMember.getMemberId());
		Map<Portfolio, PortfolioGainHistory> portfolioGainHistoryMap = portfolios.stream()
			.collect(Collectors.toMap(
				portfolio -> portfolio,
				portfolio -> portfolioGainHistoryRepository.findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc(
					portfolio.getId(), LocalDateTime.now()).orElseGet(PortfolioGainHistory::empty)
			));

		return PortfoliosResponse.of(portfolios, portfolioGainHistoryMap, currentPriceManager);
	}

	public boolean hasAuthorizationBy(Long portfolioId, Long memberId) {
		Portfolio portfolio = portfolioRepository.findById(portfolioId)
			.orElseThrow(() -> new NotFoundResourceException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
		return portfolio.hasAuthorization(memberId);
	}
}
