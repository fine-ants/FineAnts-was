package co.fineants.api.domain.holding.domain.chart;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioPieChartItem;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.stock.domain.entity.Stock;

class PieChartTest extends AbstractContainerBaseTest {

	@Autowired
	private PieChart chart;

	@Autowired
	private CurrentPriceRedisRepository currentPriceRedisRepository;

	@DisplayName("사용자는 포트폴리오의 파이 차트를 요청한다")
	@Test
	void createPieChart() {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock stock = createSamsungStock();
		Stock stock2 = createDongwhaPharmStock();
		PortfolioHolding holding1 = PortfolioHolding.of(portfolio, stock);
		PortfolioHolding holding2 = PortfolioHolding.of(portfolio, stock2);

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(5);
		Money purchasePerShare = Money.won(10000);
		String memo = "첫구매";
		PurchaseHistory purchaseHistory1 = createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo,
			holding1);

		numShares = Count.from(5);
		purchasePerShare = Money.won(20000);
		PurchaseHistory purchaseHistory2 = createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo,
			holding2);

		holding1.addPurchaseHistory(purchaseHistory1);
		holding2.addPurchaseHistory(purchaseHistory2);

		portfolio.addHolding(holding1);
		portfolio.addHolding(holding2);

		currentPriceRedisRepository.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 20000L));
		currentPriceRedisRepository.savePrice(KisCurrentPrice.create(stock2.getTickerSymbol(), 20000L));
		// when
		List<PortfolioPieChartItem> items = chart.createItemsBy(portfolio);

		// then
		assertThat(items)
			.asList()
			.hasSize(3)
			.extracting("name", "valuation", "totalGain")
			.usingComparatorForType(Money::compareTo, Money.class)
			.containsExactly(
				Tuple.tuple("현금", Money.won(850000), Money.zero()),
				Tuple.tuple("삼성전자보통주", Money.won(100000), Money.won(50000)),
				Tuple.tuple("동화약품보통주", Money.won(100000), Money.zero()));
	}
}
