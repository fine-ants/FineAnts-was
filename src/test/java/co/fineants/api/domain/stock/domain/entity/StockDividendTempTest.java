package co.fineants.api.domain.stock.domain.entity;

import java.time.LocalDate;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;

class StockDividendTempTest {
	@DisplayName("보유 주식 수에 따른 배당금 합계를 계산한다")
	@Test
	void calculateDividendSum() {
		StockDividendTemp stockDividendTemp = TestDataFactory.createSamsungStockDividendTemp();
		Count numShares = Count.from(10);

		Expression sum = stockDividendTemp.calculateDividendSum(numShares);

		Assertions.assertThat(sum).isEqualTo(Money.won(3610));
	}

	@DisplayName("보유 주식 수가 0이면 배당금 합계는 0원이다")
	@Test
	void calculateDividendSum_whenNumSharesIsZero_thenReturnZero() {
		StockDividendTemp stockDividendTemp = TestDataFactory.createSamsungStockDividendTemp();
		Count numShares = Count.zero();

		Expression sum = stockDividendTemp.calculateDividendSum(numShares);

		Assertions.assertThat(sum).isEqualTo(Money.won(0));
	}

	@DisplayName("현재 날짜가 배당금 지급일이 속한 달과 같으면 true를 반환한다")
	@Test
	void isCurrentMonthPaymentDate() {
		StockDividendTemp stockDividendTemp = TestDataFactory.createSamsungStockDividendTemp();
		LocalDate today = LocalDate.of(2023, 5, 1);

		boolean actual = stockDividendTemp.isCurrentMonthPaymentDate(today);

		Assertions.assertThat(actual).isTrue();
	}

	@DisplayName("구매일자가 배당락일 이전이면 true를 반환한다")
	@Test
	void isPurchaseDateBeforeExDividendDate() {
		StockDividendTemp stockDividendTemp = TestDataFactory.createSamsungStockDividendTemp();
		LocalDate purchaseDate = LocalDate.of(2023, 3, 29);
		Member member = TestDataFactory.createMember();
		Portfolio portfolio = TestDataFactory.createPortfolio(member);
		Stock stock = TestDataFactory.createSamsungStock();
		PortfolioHolding holding = TestDataFactory.createPortfolioHolding(portfolio, stock);
		PurchaseHistory history = TestDataFactory.createPurchaseHistory(purchaseDate, holding);

		boolean actual = stockDividendTemp.isPurchaseDateBeforeExDividendDate(history);

		Assertions.assertThat(actual).isTrue();
	}
}
