package co.fineants.api.domain.holding.domain.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.member.domain.Member;
import co.fineants.stock.domain.Stock;

class PortfolioHoldingTest {

	@DisplayName("포트폴리오 종목에 포트폴리오를 설정한다")
	@Test
	void should_set_other_portfolio() {
		// given
		Member member = TestDataFactory.createMember();
		Portfolio portfolio = TestDataFactory.createPortfolio(member);
		Stock stock = TestDataFactory.createSamsungStock();
		PortfolioHolding holding = TestDataFactory.createPortfolioHolding(portfolio, stock);

		Portfolio other = TestDataFactory.createPortfolio(member, "other");
		// when
		holding.setPortfolio(other);
		// then
		Assertions.assertThat(portfolio.getPortfolioHoldings()).isEmpty();
		Assertions.assertThat(other.getPortfolioHoldings())
			.hasSize(1)
			.containsExactlyInAnyOrder(TestDataFactory.createPortfolioHolding(other, stock));
		Assertions.assertThat(holding.getPortfolio()).isEqualTo(other);
	}
}
