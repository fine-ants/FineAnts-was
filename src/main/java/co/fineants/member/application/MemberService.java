package co.fineants.member.application;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.fcm.repository.FcmRepository;
import co.fineants.api.domain.gainhistory.repository.PortfolioGainHistoryRepository;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
import co.fineants.api.domain.stock_target_price.repository.StockTargetPriceRepository;
import co.fineants.api.domain.stock_target_price.repository.TargetPriceNotificationRepository;
import co.fineants.api.domain.watchlist.domain.entity.WatchList;
import co.fineants.api.domain.watchlist.domain.entity.WatchStock;
import co.fineants.api.domain.watchlist.repository.WatchListRepository;
import co.fineants.api.domain.watchlist.repository.WatchStockRepository;
import co.fineants.api.global.errors.exception.business.MemberNotFoundException;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.member.domain.NotificationPreference;
import co.fineants.member.presentation.dto.response.ProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {

	private final MemberRepository memberRepository;
	private final WatchListRepository watchListRepository;
	private final WatchStockRepository watchStockRepository;
	private final PortfolioHoldingRepository portfolioHoldingRepository;
	private final PortfolioRepository portfolioRepository;
	private final PortfolioGainHistoryRepository portfolioGainHistoryRepository;
	private final PurchaseHistoryRepository purchaseHistoryRepository;
	private final NotificationRepository notificationRepository;
	private final FcmRepository fcmRepository;
	private final StockTargetPriceRepository stockTargetPriceRepository;
	private final TargetPriceNotificationRepository targetPriceNotificationRepository;

	@Transactional
	public void deleteMember(Long memberId) {
		Member member = findMember(memberId);
		List<Portfolio> portfolios = portfolioRepository.findAllByMemberId(memberId);
		List<PortfolioHolding> portfolioHoldings = new ArrayList<>();
		portfolios.forEach(
			portfolio -> portfolioHoldings.addAll(portfolioHoldingRepository.findAllByPortfolio(portfolio)));
		// 포트폴리오에 속한 모든 포트폴리오 손익 내역 데이터 삭제
		List<Long> portfolioIds = portfolios.stream()
			.map(Portfolio::getId)
			.toList();
		portfolioGainHistoryRepository.deleteAllByPortfolioIds(portfolioIds);
		purchaseHistoryRepository.deleteAllByPortfolioHoldingIdIn(
			portfolioHoldings.stream().map(PortfolioHolding::getId).toList());
		portfolioHoldingRepository.deleteAll(portfolioHoldings);
		portfolioRepository.deleteAll(portfolios);
		List<WatchList> watchList = watchListRepository.findByMember(member);
		List<WatchStock> watchStocks = new ArrayList<>();
		watchList.forEach(w -> watchStocks.addAll(watchStockRepository.findByWatchList(w)));
		watchStockRepository.deleteAll(watchStocks);
		watchListRepository.deleteAll(watchList);
		fcmRepository.deleteAllByMemberId(member.getId());
		List<StockTargetPrice> stockTargetPrices = stockTargetPriceRepository.findAllByMemberId(member.getId());
		targetPriceNotificationRepository.deleteAllByStockTargetPrices(stockTargetPrices);
		stockTargetPriceRepository.deleteAllByMemberId(member.getId());
		notificationRepository.deleteAllByMemberId(member.getId());
		memberRepository.delete(member);
	}

	private Member findMember(Long id) {
		return memberRepository.findById(id)
			.orElseThrow(() -> new MemberNotFoundException(id.toString()));
	}

	@Transactional(readOnly = true)
	@Secured("ROLE_USER")
	public ProfileResponse readProfile(Long memberId) {
		Member member = findMember(memberId);
		NotificationPreference preference = member.getNotificationPreference();
		return ProfileResponse.from(member, ProfileResponse.NotificationPreference.from(preference));
	}
}
