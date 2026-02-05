package co.fineants.api.domain.portfolio.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.gainhistory.repository.PortfolioGainHistoryRepository;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.dto.request.PortfolioCreateRequest;
import co.fineants.api.domain.portfolio.domain.dto.request.PortfolioModifyRequest;
import co.fineants.api.domain.portfolio.domain.dto.response.PortFolioCreateResponse;
import co.fineants.api.domain.portfolio.domain.dto.response.PortFolioItem;
import co.fineants.api.domain.portfolio.domain.dto.response.PortfolioModifyResponse;
import co.fineants.api.domain.portfolio.domain.dto.response.PortfolioNameItem;
import co.fineants.api.domain.portfolio.domain.dto.response.PortfolioNameResponse;
import co.fineants.api.domain.portfolio.domain.dto.response.PortfoliosResponse;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.properties.PortfolioProperties;
import co.fineants.api.domain.portfolio.repository.PortfolioPropertiesRepository;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.global.common.authorized.Authorized;
import co.fineants.api.global.common.authorized.service.PortfolioAuthorizedService;
import co.fineants.api.global.common.resource.ResourceId;
import co.fineants.api.global.common.resource.ResourceIds;
import co.fineants.api.global.errors.exception.business.MemberNotFoundException;
import co.fineants.api.global.errors.exception.business.PortfolioInvalidInputException;
import co.fineants.api.global.errors.exception.business.PortfolioNameDuplicateException;
import co.fineants.api.global.errors.exception.business.PortfolioNotFoundException;
import co.fineants.api.global.errors.exception.business.SecuritiesFirmInvalidInputException;
import co.fineants.api.global.errors.exception.domain.DomainException;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.stock.domain.Stock;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PortfolioService {

	private final PortfolioRepository portfolioRepository;
	private final MemberRepository memberRepository;
	private final PortfolioHoldingRepository portfolioHoldingRepository;
	private final PurchaseHistoryRepository purchaseHistoryRepository;
	private final PortfolioGainHistoryRepository portfolioGainHistoryRepository;
	private final CurrentPriceRedisRepository currentPriceRedisRepository;
	private final PortfolioPropertiesRepository portfolioPropertiesRepository;
	private final PortfolioProperties properties;
	private final PortfolioCalculator calculator;

	@Transactional
	@Secured("ROLE_USER")
	@CacheEvict(value = "myAllPortfolioNames", key = "#memberId")
	public PortFolioCreateResponse createPortfolio(PortfolioCreateRequest request, Long memberId) throws
		PortfolioNameDuplicateException,
		PortfolioInvalidInputException {
		validateSecuritiesFirm(request.getSecuritiesFirm());

		Member member = findMember(memberId);

		validateUniquePortfolioName(request.getName(), member);
		Portfolio portfolio;
		try {
			portfolio = request.toEntity(member, properties);
		} catch (DomainException e) {
			throw new PortfolioInvalidInputException(request.toString(), e);
		}
		return PortFolioCreateResponse.from(portfolioRepository.save(portfolio));
	}

	private Member findMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberNotFoundException(memberId.toString()));
	}

	private void validateSecuritiesFirm(String securitiesFirm) {
		if (!portfolioPropertiesRepository.contains(securitiesFirm)) {
			throw new SecuritiesFirmInvalidInputException(securitiesFirm);
		}
	}

	private void validateUniquePortfolioName(String name, Member member) throws PortfolioNameDuplicateException {
		if (portfolioRepository.findByNameAndMember(name, member).isPresent()) {
			throw new PortfolioNameDuplicateException(name);
		}
	}

	@Transactional
	@CacheEvict(value = "myAllPortfolioNames", key = "#memberId")
	@Authorized(serviceClass = PortfolioAuthorizedService.class)
	@Secured("ROLE_USER")
	public PortfolioModifyResponse updatePortfolio(PortfolioModifyRequest request, @ResourceId Long portfolioId,
		Long memberId) throws PortfolioNameDuplicateException, PortfolioInvalidInputException {
		log.info("포트폴리오 수정 서비스 요청 : request={}, portfolioId={}, memberId={}", request, portfolioId, memberId);
		Member member = findMember(memberId);
		Portfolio originalPortfolio = findPortfolio(portfolioId);
		Portfolio changePortfolio;
		try {
			changePortfolio = request.toEntity(member, properties);
		} catch (DomainException e) {
			throw new PortfolioInvalidInputException(request.toString(), e);
		}

		if (!originalPortfolio.equalName(changePortfolio)) {
			validateUniquePortfolioName(changePortfolio.name(), member);
		}
		originalPortfolio.change(changePortfolio);

		log.info("변경된 포트폴리오 결과 : {}", originalPortfolio);
		return PortfolioModifyResponse.from(changePortfolio);
	}

	@Transactional
	@CacheEvict(value = "myAllPortfolioNames", key = "#memberId")
	@Authorized(serviceClass = PortfolioAuthorizedService.class)
	@Secured("ROLE_USER")
	public void deletePortfolio(@ResourceId Long portfolioId, Long memberId) {
		log.info("포트폴리오 삭제 서비스 요청 : portfolioId={}, memberId={}", portfolioId, memberId);

		Portfolio findPortfolio = findPortfolio(portfolioId);
		List<Long> portfolioHoldingIds = portfolioHoldingRepository.findAllByPortfolio(findPortfolio).stream()
			.map(PortfolioHolding::getId)
			.toList();

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
	@CacheEvict(value = "myAllPortfolioNames", key = "#memberId")
	@Authorized(serviceClass = PortfolioAuthorizedService.class)
	@Secured("ROLE_USER")
	public void deletePortfolios(@ResourceIds List<Long> portfolioIds, @NotNull Long memberId) {
		log.info("portfolio multiple delete service request: portfolioIds={}, memberId={}", portfolioIds, memberId);
		for (Long portfolioId : portfolioIds) {
			Portfolio portfolio = findPortfolio(portfolioId);
			List<Long> portfolioStockIds = portfolioHoldingRepository.findAllByPortfolio(portfolio).stream()
				.map(PortfolioHolding::getId)
				.toList();
			purchaseHistoryRepository.deleteAllByPortfolioHoldingIdIn(portfolioStockIds);
			portfolioHoldingRepository.deleteAllByPortfolioId(portfolio.getId());
			portfolioGainHistoryRepository.deleteAllByPortfolioId(portfolioId);
			portfolioRepository.deleteById(portfolio.getId());
		}
	}

	@Secured("ROLE_USER")
	@Authorized(serviceClass = PortfolioAuthorizedService.class)
	public Portfolio findPortfolio(@ResourceId Long portfolioId) {
		return portfolioRepository.findById(portfolioId)
			.orElseThrow(() -> new PortfolioNotFoundException(portfolioId.toString()));
	}

	@Secured("ROLE_USER")
	public PortfoliosResponse readMyAllPortfolio(Long memberId) {
		List<Portfolio> portfolios = portfolioRepository.findAllByMemberIdOrderByIdDesc(memberId);
		Map<Portfolio, PortfolioGainHistory> portfolioGainHistoryMap = portfolios.stream()
			.collect(Collectors.toMap(
				portfolio -> portfolio,
				portfolio ->
					portfolioGainHistoryRepository.findFirstLatestPortfolioGainHistory(
							portfolio.getId(), LocalDateTime.now(), PageRequest.of(0, 1))
						.stream()
						.findAny()
						.orElseGet(() -> PortfolioGainHistory.empty(portfolio))
			));

		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		List<PortFolioItem> items = new ArrayList<>();
		for (Portfolio portfolio : portfolios) {
			PortfolioGainHistory prevHistory = portfolioGainHistoryMap.get(portfolio);
			Money totalGain = calculator.calTotalGainBy(portfolio).reduce(bank, to);
			Percentage totalGainRate = calculator.calTotalGainRateBy(portfolio)
				.toPercentage(bank, to);
			Money dailyGain = calculator.calDailyGain(prevHistory, portfolio).reduce(bank, to);
			Percentage dailyGainRate = calculator.calDailyGainRateBy(prevHistory, portfolio).toPercentage(bank, to);
			Money currentValuation = calculator.calTotalCurrentValuationBy(portfolio).reduce(bank, to);
			Money currentMonthDividend = calculator.calCurrentMonthDividendBy(portfolio).reduce(bank, to);

			PortFolioItem item = PortFolioItem.builder()
				.id(portfolio.getId())
				.securitiesFirm(portfolio.securitiesFirm())
				.name(portfolio.name())
				.budget(portfolio.getBudget())
				.totalGain(totalGain)
				.totalGainRate(totalGainRate)
				.dailyGain(dailyGain)
				.dailyGainRate(dailyGainRate)
				.currentValuation(currentValuation)
				.expectedMonthlyDividend(currentMonthDividend)
				.numShares(portfolio.numberOfShares())
				.dateCreated(portfolio.getCreateAt())
				.build();
			items.add(item);
		}

		return new PortfoliosResponse(items);
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "myAllPortfolioNames", key = "#memberId")
	@Secured("ROLE_USER")
	public PortfolioNameResponse readMyAllPortfolioNames(@NotNull Long memberId) {
		List<PortfolioNameItem> items = portfolioRepository.findAllByMemberIdOrderByIdDesc(memberId).stream()
			.sorted(Comparator.comparing(Portfolio::getCreateAt).reversed())
			.map(PortfolioNameItem::from)
			.toList();
		return PortfolioNameResponse.from(items);
	}

	// 포트폴리오에 등록된 종목의 티커 집합을 반환하는 서비스
	@Transactional(readOnly = true)
	@Secured("ROLE_USER")
	@Authorized(serviceClass = PortfolioAuthorizedService.class)
	public Set<String> getTickerSymbolsInPortfolio(@ResourceId Long portfolioId) {
		Portfolio portfolio = findPortfolio(portfolioId);
		return portfolio.getPortfolioHoldings().stream()
			.map(PortfolioHolding::getStock)
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toSet());
	}
}
