package co.fineants.api.domain.portfolio.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.gainhistory.repository.PortfolioGainHistoryRepository;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.dto.request.PortfolioCreateRequest;
import co.fineants.api.domain.portfolio.domain.dto.request.PortfolioModifyRequest;
import co.fineants.api.domain.portfolio.domain.dto.response.PortFolioCreateResponse;
import co.fineants.api.domain.portfolio.domain.dto.response.PortfolioModifyResponse;
import co.fineants.api.domain.portfolio.domain.dto.response.PortfolioNameItem;
import co.fineants.api.domain.portfolio.domain.dto.response.PortfoliosResponse;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.properties.PortfolioProperties;
import co.fineants.api.domain.portfolio.repository.PortfolioPropertiesRepository;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.global.common.authorized.Authorized;
import co.fineants.api.global.common.authorized.service.PortfolioAuthorizedService;
import co.fineants.api.global.common.paging.page.CustomPageDto;
import co.fineants.api.global.common.paging.page.CustomPageable;
import co.fineants.api.global.common.paging.slice.CustomSliceDto;
import co.fineants.api.global.common.resource.ResourceId;
import co.fineants.api.global.common.resource.ResourceIds;
import co.fineants.api.global.errors.exception.business.MemberNotFoundException;
import co.fineants.api.global.errors.exception.business.PortfolioInvalidInputException;
import co.fineants.api.global.errors.exception.business.PortfolioNameDuplicateException;
import co.fineants.api.global.errors.exception.business.PortfolioNotFoundException;
import co.fineants.api.global.errors.exception.business.SecuritiesFirmInvalidInputException;
import co.fineants.api.global.errors.exception.domain.DomainException;
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

		return PortfoliosResponse.of(portfolios, portfolioGainHistoryMap, currentPriceRedisRepository, calculator);
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "myAllPortfolioNames", key = "#memberId")
	@Secured("ROLE_USER")
	public CustomPageDto<Portfolio, PortfolioNameItem> getPagedPortfolioNames(@NotNull Long memberId,
		@NotNull Pageable pageable) {
		Page<Portfolio> page = portfolioRepository.findAllByMemberIdAndPageable(memberId, pageable);
		List<PortfolioNameItem> items = page.stream()
			.map(PortfolioNameItem::from)
			.toList();
		CustomPageable customPageable = CustomPageable.from(pageable);
		return new CustomPageDto<>(customPageable, page, items);
	}

	@Cacheable(
		value = "myFirstPagePortfolioNames",
		key = "#memberId",
		condition = "#pageable.pageNumber == 0"
	)
	@Transactional(readOnly = true)
	@Secured("ROLE_USER")
	public CustomSliceDto<Portfolio, PortfolioNameItem> getPagedPortfolioNames_withSlice(@NotNull Long memberId,
		@NotNull Pageable pageable) {
		Slice<Portfolio> slice = portfolioRepository.findAllByMemberIdAndPageable_withSlice(memberId, pageable);
		List<PortfolioNameItem> items = slice.stream()
			.map(PortfolioNameItem::from)
			.toList();
		CustomPageable customPageable = CustomPageable.from(pageable);
		return new CustomSliceDto(customPageable, slice, items);
	}
}
