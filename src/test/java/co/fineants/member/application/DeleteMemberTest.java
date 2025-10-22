package co.fineants.member.application;

import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.infrastructure.StockSpringDataJpaRepository;
import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
import co.fineants.api.domain.stock_target_price.repository.StockTargetPriceRepository;
import co.fineants.api.domain.stock_target_price.repository.TargetPriceNotificationRepository;
import co.fineants.api.domain.watchlist.domain.entity.WatchList;
import co.fineants.api.domain.watchlist.repository.WatchListRepository;
import co.fineants.api.domain.watchlist.repository.WatchStockRepository;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;

class DeleteMemberTest extends AbstractContainerBaseTest {

	@Autowired
	private DeleteMember deleteMember;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private PortfolioHoldingRepository portfolioHoldingRepository;

	@Autowired
	private PurchaseHistoryRepository purchaseHistoryRepository;

	@Autowired
	private StockTargetPriceRepository stockTargetPriceRepository;

	@Autowired
	private TargetPriceNotificationRepository targetPriceNotificationRepository;
	@Autowired
	private StockSpringDataJpaRepository stockRepository;

	@Autowired
	private WatchListRepository watchListRepository;

	@Autowired
	private WatchStockRepository watchStockRepository;

	@DisplayName("사용자는 계정을 삭제한다")
	@Test
	void deleteMember() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		createStockDividendWith(stock.getTickerSymbol()).forEach(stock::addStockDividend);
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(3);
		Money purchasePricePerShare = Money.won(50000);
		String memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePricePerShare, memo, portfolioHolding));
		StockTargetPrice stockTargetPrice = stockTargetPriceRepository.save(createStockTargetPrice(member, stock));
		targetPriceNotificationRepository.save(createTargetPriceNotification(stockTargetPrice));
		WatchList watchList = watchListRepository.save(createWatchList(member));
		watchStockRepository.save(createWatchStock(watchList, stock));
		// when
		deleteMember.delete(member.getId());

		// then
		Assertions.assertThat(memberRepository.findById(member.getId())).isEmpty();
	}
}
