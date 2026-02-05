package co.fineants.stock.domain.calculator;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.dividend.domain.entity.DividendDates;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.member.domain.Member;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockDividend;

class StockDividendCalculatorTest {
	private Money toWon(Expression expression) {
		return expression.reduce(Bank.getInstance(), Currency.KRW);
	}

	@DisplayName("객체 생성 테스트")
	@Test
	void canCreated() {
		// when
		DividendCalculator calculator = new StockDividendCalculator();

		// then
		Assertions.assertThat(calculator).isNotNull();
	}

	@DisplayName("배당 월 리스트 계산 - 매개변수가 null이면 예외를 던진다")
	@Test
	void calculateDividendMonths_ThrowsException_WhenDividendsIsNull() {
		// given
		DividendCalculator calculator = new StockDividendCalculator();
		List<StockDividend> dividends = Collections.emptyList();
		LocalDate baseDate = LocalDate.of(2023, 6, 1);
		// when & then
		Assertions.assertThatThrownBy(() -> calculator.calculateDividendMonths(null, baseDate))
			.isInstanceOf(NullPointerException.class);
		Assertions.assertThatThrownBy(() -> calculator.calculateDividendMonths(dividends, null))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("배당 월 리스트 계산 - 배당금 리스트가 비어있으면 빈 리스트를 반환한다")
	@Test
	void calculateDividendMonths_ReturnsEmptyList_WhenDividendsIsEmpty() {
		// given
		DividendCalculator calculator = new StockDividendCalculator();
		List<StockDividend> dividends = Collections.emptyList();
		LocalDate baseDate = LocalDate.of(2023, 6, 1);
		// when
		List<Integer> actual = calculator.calculateDividendMonths(dividends, baseDate);
		// then
		Assertions.assertThat(actual).isEmpty();
	}

	@DisplayName("배당 월 리스트 계산 - 배당 리스트 원소가 한개 있으면 해당 월 리스트를 반환한다")
	@Test
	void calculateDividendMonths_ReturnsMonthList_WhenDividendsHasOneElement() {
		// given
		DividendCalculator calculator = new StockDividendCalculator();
		LocalDate recordDate = LocalDate.of(2023, 3, 31);
		LocalDate exDividendDate = LocalDate.of(2023, 4, 1);
		LocalDate paymentDate = LocalDate.of(2023, 5, 1);
		DividendDates dividendDates = DividendDates.of(recordDate, exDividendDate, paymentDate);

		StockDividend dividend = TestDataFactory.createStockDividend(dividendDates);
		List<StockDividend> dividends = List.of(dividend);
		LocalDate baseDate = LocalDate.of(2023, 6, 1);
		// when
		List<Integer> actual = calculator.calculateDividendMonths(dividends, baseDate);
		// then
		List<Integer> expected = List.of(5);
		Assertions.assertThat(actual).isEqualTo(expected);
	}

	@DisplayName("배당 월 리스트 계산 - 배당 리스트 원소가 여러개 있으면 해당 월 리스트를 반환한다")
	@Test
	void calculateDividendMonths_ReturnsMonthList_WhenDividendsHasMultipleElements() {
		// given
		DividendCalculator calculator = new StockDividendCalculator();

		DividendDates dividendDates1 = DividendDates.of(
			LocalDate.of(2023, 3, 31),
			LocalDate.of(2023, 4, 1),
			LocalDate.of(2023, 5, 1)
		);
		DividendDates dividendDates2 = DividendDates.of(
			LocalDate.of(2023, 6, 30),
			LocalDate.of(2023, 7, 1),
			LocalDate.of(2023, 8, 1)
		);
		DividendDates dividendDates3 = DividendDates.of(
			LocalDate.of(2022, 9, 30),
			LocalDate.of(2022, 10, 1),
			LocalDate.of(2022, 11, 1)
		);

		StockDividend dividend1 = TestDataFactory.createStockDividend(dividendDates1);
		StockDividend dividend2 = TestDataFactory.createStockDividend(dividendDates2);
		StockDividend dividend3 = TestDataFactory.createStockDividend(dividendDates3);

		List<StockDividend> dividends = List.of(dividend1, dividend2, dividend3);
		LocalDate baseDate = LocalDate.of(2023, 6, 1);
		// when
		List<Integer> actual = calculator.calculateDividendMonths(dividends, baseDate);
		// then
		List<Integer> expected = List.of(5, 8);
		Assertions.assertThat(actual).isEqualTo(expected);
	}

	@DisplayName("현재 달 배당 리스트 계산 - 종목 배당 리스트가 비어있으면 빈 리스트를 반환한다")
	@Test
	void calculateCurrentMonthStockDividends_ReturnsEmptyList_WhenDividendsIsEmpty() {
		// given
		DividendCalculator calculator = new StockDividendCalculator();
		List<StockDividend> dividends = Collections.emptyList();
		LocalDate baseDate = LocalDate.of(2023, 6, 1);
		// when
		List<StockDividend> actual = calculator.calculateCurrentMonthStockDividends(dividends, baseDate);
		// then
		Assertions.assertThat(actual).isEmpty();
	}

	@DisplayName("현재 달 배당 리스트 계산 - 매개변수가 null이면 예외를 던진다")
	@Test
	void calculateCurrentMonthStockDividends_ThrowsException_WhenDividendsIsNull() {
		// given
		DividendCalculator calculator = new StockDividendCalculator();
		List<StockDividend> dividends = Collections.emptyList();
		LocalDate baseDate = LocalDate.of(2023, 6, 1);
		// when & then
		Assertions.assertThatThrownBy(() -> calculator.calculateCurrentMonthStockDividends(null, baseDate))
			.isInstanceOf(NullPointerException.class)
			.hasMessage("dividends must not be null");
		Assertions.assertThatThrownBy(() -> calculator.calculateCurrentMonthStockDividends(dividends, null))
			.isInstanceOf(NullPointerException.class)
			.hasMessage("baseDate must not be null");
	}

	@DisplayName("현재 달 배당 리스트 계산 - 조건이 맞는 배당 리스트의 원소가 한개 있으면 한개의 종목 배당 데이터를 반환한다")
	@Test
	void calculateCurrentMonthStockDividends_ReturnsStockDividendList_WhenDividendsHasOneElementMatchingCondition() {
		// given
		DividendCalculator calculator = new StockDividendCalculator();

		DividendDates dividendDates1 = DividendDates.of(
			LocalDate.of(2023, 3, 31),
			LocalDate.of(2023, 4, 1),
			LocalDate.of(2023, 6, 15)
		);

		StockDividend dividend1 = TestDataFactory.createStockDividend(dividendDates1);

		List<StockDividend> dividends = List.of(dividend1);
		LocalDate baseDate = LocalDate.of(2023, 6, 1);
		// when
		List<StockDividend> actual = calculator.calculateCurrentMonthStockDividends(dividends, baseDate);
		// then
		List<StockDividend> expected = List.of(dividend1);
		Assertions.assertThat(actual).isEqualTo(expected);
	}

	@DisplayName("현재 달 배당 리스트 계산 - 조건이 맞는 배당 리스트의 원소가 여러개 있으면 여러개의 종목 배당 데이터를 반환한다")
	@Test
	void calculateCurrentMonthStockDividends_ReturnsStockDividendList_WhenDividendsHasMultipleElementsMatchingCondition() {
		// given
		DividendCalculator calculator = new StockDividendCalculator();

		DividendDates dividendDates1 = DividendDates.of(
			LocalDate.of(2023, 3, 31),
			LocalDate.of(2023, 4, 1),
			LocalDate.of(2023, 6, 15)
		);
		DividendDates dividendDates2 = DividendDates.of(
			LocalDate.of(2023, 5, 31),
			LocalDate.of(2023, 6, 1),
			LocalDate.of(2023, 6, 20)
		);
		DividendDates dividendDates3 = DividendDates.of(
			LocalDate.of(2022, 9, 30),
			LocalDate.of(2022, 10, 1),
			LocalDate.of(2022, 11, 1)
		);

		StockDividend dividend1 = TestDataFactory.createStockDividend(dividendDates1);
		StockDividend dividend2 = TestDataFactory.createStockDividend(dividendDates2);
		StockDividend dividend3 = TestDataFactory.createStockDividend(dividendDates3);

		List<StockDividend> dividends = List.of(dividend1, dividend2, dividend3);
		LocalDate baseDate = LocalDate.of(2023, 6, 1);
		// when
		List<StockDividend> actual = calculator.calculateCurrentMonthStockDividends(dividends, baseDate);
		// then
		List<StockDividend> expected = List.of(dividend1, dividend2);
		Assertions.assertThat(actual).isEqualTo(expected);
	}

	@DisplayName("현재 달 예상 배당금 계산 - 종목 배당 리스트가 비어있으면 0을 반환한다")
	@Test
	void calCurrentMonthExpectedDividend_ReturnsZero_WhenDividendsIsEmpty() {
		// given
		DividendCalculator calculator = new StockDividendCalculator();
		List<StockDividend> dividends = Collections.emptyList();
		List<PurchaseHistory> histories = Collections.emptyList();
		// when
		Expression actual = calculator.calCurrentMonthExpectedDividend(dividends, histories);
		// then
		Assertions.assertThat(toWon(actual)).isEqualTo(Money.zero());
	}

	@DisplayName("현재 달 예상 배당금 계산 - 파라미터가 null이면 예외를 던진다")
	@Test
	void calCurrentMonthExpectedDividend_ThrowsException_WhenDividendsIsNull() {
		// given
		DividendCalculator calculator = new StockDividendCalculator();
		List<StockDividend> dividends = Collections.emptyList();
		List<PurchaseHistory> histories = Collections.emptyList();
		// when & then
		Assertions.assertThatThrownBy(() -> calculator.calCurrentMonthExpectedDividend(null, histories))
			.isInstanceOf(NullPointerException.class)
			.hasMessage("dividends must not be null");
		Assertions.assertThatThrownBy(() -> calculator.calCurrentMonthExpectedDividend(dividends, null))
			.isInstanceOf(NullPointerException.class)
			.hasMessage("histories must not be null");
	}

	@DisplayName("현재 달 예상 배당금 계산 - 종목 배당 리스트와 매입 이력 리스트에 만족하는 원소가 있으면 합계를 반환한다")
	@Test
	void calCurrentMonthExpectedDividend_ReturnsSum_WhenDividendsAndHistoriesHaveMatchingElements() {
		// given
		DividendCalculator calculator = new StockDividendCalculator();

		DividendDates dividendDates1 = DividendDates.of(
			LocalDate.of(2023, 3, 31),
			LocalDate.of(2023, 4, 1),
			LocalDate.of(2023, 6, 15)
		);
		DividendDates dividendDates2 = DividendDates.of(
			LocalDate.of(2023, 5, 31),
			LocalDate.of(2023, 6, 1),
			LocalDate.of(2023, 6, 20)
		);

		StockDividend dividend1 = TestDataFactory.createStockDividend(dividendDates1);
		StockDividend dividend2 = TestDataFactory.createStockDividend(dividendDates2);

		List<StockDividend> dividends = List.of(dividend1, dividend2);

		LocalDate purchaseDate = LocalDate.of(2023, 1, 1);
		Member member = TestDataFactory.createMember();
		Portfolio portfolio = TestDataFactory.createPortfolio(member);
		Stock stock = TestDataFactory.createSamsungStock();
		PortfolioHolding holding = TestDataFactory.createPortfolioHolding(portfolio, stock);
		PurchaseHistory history1 = TestDataFactory.createPurchaseHistory(purchaseDate, holding);
		PurchaseHistory history2 = TestDataFactory.createPurchaseHistory(purchaseDate, holding);
		List<PurchaseHistory> histories = List.of(history1, history2);

		// when
		Expression actual = calculator.calCurrentMonthExpectedDividend(dividends, histories);

		// then
		Assertions.assertThat(toWon(actual)).isEqualTo(Money.won(20_000));
	}

}
