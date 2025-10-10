package co.fineants.api.domain.stock.domain.entity;

import java.time.LocalDate;
import java.time.Month;

import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
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
	@NotNull
	private PurchaseHistory createPurchaseHistory(LocalDate purchaseDate) {
		Member member = TestDataFactory.createMember();
		Portfolio portfolio = TestDataFactory.createPortfolio(member);
		Stock stock = TestDataFactory.createSamsungStock();
		PortfolioHolding holding = TestDataFactory.createPortfolioHolding(portfolio, stock);
		return TestDataFactory.createPurchaseHistory(purchaseDate, holding);
	}

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
		PurchaseHistory history = createPurchaseHistory(LocalDate.of(2023, 3, 29));

		boolean actual = stockDividendTemp.isPurchaseDateBeforeExDividendDate(history);

		Assertions.assertThat(actual).isTrue();
	}

	@DisplayName("현재 날짜가 배당금 지급일이 속한 연도와 같으면 true를 반환한다")
	@Test
	void isCurrentYearPaymentDate() {
		StockDividendTemp stockDividendTemp = TestDataFactory.createSamsungStockDividendTemp();
		LocalDate today = LocalDate.of(2023, 5, 1);

		boolean actual = stockDividendTemp.isCurrentYearPaymentDate(today);

		Assertions.assertThat(actual).isTrue();
	}

	@DisplayName("구매 이력이 배당금 수령 조건을 만족하면 true를 반환한다")
	@Test
	void isSatisfiedBy() {
		StockDividendTemp stockDividendTemp = TestDataFactory.createSamsungStockDividendTemp();
		PurchaseHistory history = createPurchaseHistory(LocalDate.of(2023, 3, 29));

		boolean satisfiedBy = stockDividendTemp.isSatisfiedBy(history);

		Assertions.assertThat(satisfiedBy).isTrue();
	}

	@DisplayName("현재 날짜가 배당금 기준일이 속한 연도와 같으면 true를 반환한다")
	@Test
	void isCurrentYearRecordDate() {
		StockDividendTemp stockDividendTemp = TestDataFactory.createSamsungStockDividendTemp();
		LocalDate today = LocalDate.of(2023, 2, 1);

		boolean currentYearRecordDate = stockDividendTemp.isCurrentYearRecordDate(today);

		Assertions.assertThat(currentYearRecordDate).isTrue();
	}

	@DisplayName("구매 이력이 배당금 수령 조건을 만족하면 true를 반환한다")
	@Test
	void canReceiveDividendOn() {
		StockDividendTemp stockDividendTemp = TestDataFactory.createSamsungStockDividendTemp();
		PurchaseHistory history = createPurchaseHistory(LocalDate.of(2023, 3, 29));

		boolean canReceiveDividendOn = stockDividendTemp.canReceiveDividendOn(history);

		Assertions.assertThat(canReceiveDividendOn).isTrue();
	}

	@DisplayName("배당금 지급일이 속한 월을 반환한다")
	@Test
	void getMonthByPaymentDate() {
		StockDividendTemp stockDividendTemp = TestDataFactory.createSamsungStockDividendTemp();

		Month monthByPaymentDate = stockDividendTemp.getMonthByPaymentDate();

		Assertions.assertThat(monthByPaymentDate).isEqualTo(java.time.Month.MAY);
	}

	@DisplayName("지난해 배당금 지급일이 속한 연도와 같으면 true를 반환한다")
	@Test
	void isLastYearPaymentDate() {
		StockDividendTemp stockDividendTemp = TestDataFactory.createSamsungStockDividendTemp();
		LocalDate lastYearLocalDate = LocalDate.of(2023, 5, 1);

		boolean lastYearPaymentDate = stockDividendTemp.isLastYearPaymentDate(lastYearLocalDate);

		Assertions.assertThat(lastYearPaymentDate).isTrue();
	}

	@DisplayName("배당금 기준일이 중복되는지 확인한다")
	@Test
	void isDuplicatedRecordDate() {
		StockDividendTemp stockDividendTemp = TestDataFactory.createSamsungStockDividendTemp();
		StockDividendTemp stockDividendTemp2 = TestDataFactory.createSamsungStockDividendTemp();
		StockDividendTemp stockDividendTemp3 = TestDataFactory.createSamsungStockDividendTemp();

		boolean actual = stockDividendTemp.isDuplicatedRecordDate(
			java.util.List.of(stockDividendTemp2, stockDividendTemp3));

		Assertions.assertThat(actual).isTrue();
	}

	@DisplayName("배당금 지급일이 속한 연도가 현재 연도와 같으면 true를 반환한다")
	@Test
	void isPaymentInCurrentYear() {
		StockDividendTemp stockDividendTemp = TestDataFactory.createSamsungStockDividendTemp();
		LocalDate localDate = LocalDate.of(2023, 5, 1);

		boolean paymentInCurrentYear = stockDividendTemp.isPaymentInCurrentYear(localDate);

		Assertions.assertThat(paymentInCurrentYear).isTrue();
	}

	@DisplayName("배당금 기준일이 주어진 날짜와 같으면 true를 반환한다")
	@Test
	void equalRecordDate() {
		StockDividendTemp stockDividendTemp = TestDataFactory.createSamsungStockDividendTemp();
		LocalDate recordDate = LocalDate.of(2023, 3, 31);

		boolean actual = stockDividendTemp.equalRecordDate(recordDate);

		Assertions.assertThat(actual).isTrue();
	}

	@DisplayName("배당금 기준일이 주어진 날짜 범위에 속하면 true를 반환한다")
	@Test
	void hasInRangeForRecordDate() {
		StockDividendTemp stockDividendTemp = TestDataFactory.createSamsungStockDividendTemp();
		LocalDate from = LocalDate.of(2023, 1, 1);
		LocalDate to = LocalDate.of(2023, 12, 31);

		boolean actual = stockDividendTemp.hasInRangeForRecordDate(from, to);

		Assertions.assertThat(actual).isTrue();
	}

	@DisplayName("배당금 지급일이 존재하면 true를 반환한다")
	@Test
	void hasPaymentDate() {
		StockDividendTemp stockDividendTemp = TestDataFactory.createSamsungStockDividendTemp();

		boolean actual = stockDividendTemp.hasPaymentDate();

		Assertions.assertThat(actual).isTrue();

	}
}
