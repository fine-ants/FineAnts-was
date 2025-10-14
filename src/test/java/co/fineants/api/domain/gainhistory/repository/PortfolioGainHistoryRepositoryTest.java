package co.fineants.api.domain.gainhistory.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.AbstractDataJpaBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.portfolio.domain.dto.response.LineChartItem;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.member.domain.Member;
import co.fineants.member.infrastructure.MemberRepository;

class PortfolioGainHistoryRepositoryTest extends AbstractDataJpaBaseTest {

	@Autowired
	private PortfolioGainHistoryRepository portfolioGainHistoryRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@DisplayName("사용자는 제일 최근의 포트폴리오 손익 내역을 조회합니다")
	@Test
	void findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc() {
		// given
		Member member = memberRepository.save(TestDataFactory.createMember());
		Portfolio portfolio = portfolioRepository.save(TestDataFactory.createPortfolio(member));
		portfolioGainHistoryRepository.save(PortfolioGainHistory.empty(portfolio));
		PortfolioGainHistory saveHistory = portfolioGainHistoryRepository.save(PortfolioGainHistory.empty(portfolio));

		// when
		PortfolioGainHistory history =
			portfolioGainHistoryRepository.findFirstLatestPortfolioGainHistory(
					portfolio.getId(),
					LocalDateTime.now(),
					PageRequest.of(0, 1)).stream()
				.findAny()
				.orElseThrow();

		// then
		assertThat(history.getId()).isEqualTo(saveHistory.getId());
	}

	@Transactional
	@DisplayName("주어진 날짜보다 같거나 작은 데이터들중 가장 최근의 데이터를 한개 조회한다")
	@Test
	void findFirstByCreateAtIsLessThanEqualOrderByCreateAtDesc() {
		// given
		Member member = memberRepository.save(TestDataFactory.createMember());
		Portfolio portfolio = portfolioRepository.save(TestDataFactory.createPortfolio(member));

		PortfolioGainHistory portfolioGainHistory1 = PortfolioGainHistory.create(
			Money.won(10000L),
			Money.won(10000L),
			Money.won(1000000L),
			Money.won(110000L),
			portfolio
		);

		PortfolioGainHistory portfolioGainHistory2 = PortfolioGainHistory.create(
			Money.won(20000L),
			Money.won(10000L),
			Money.won(1000000L),
			Money.won(120000L),
			portfolio
		);
		portfolioGainHistoryRepository.save(portfolioGainHistory1);
		portfolioGainHistoryRepository.save(portfolioGainHistory2);

		// when
		PortfolioGainHistory result =
			portfolioGainHistoryRepository.findFirstLatestPortfolioGainHistory(
					portfolio.getId(), LocalDateTime.now(), PageRequest.of(0, 1))
				.stream()
				.findAny()
				.orElseThrow();

		// then
		assertThat(result.getCurrentValuation()).isEqualByComparingTo(Money.won(120000L));
	}

	@DisplayName("포트폴리오의 일별 총 금액을 조회한다")
	@Test
	void findDailyTotalAmountByPortfolioId() {
		// given
		Member member = memberRepository.save(TestDataFactory.createMember());
		Portfolio portfolio = portfolioRepository.save(TestDataFactory.createPortfolio(member));

		PortfolioGainHistory portfolioGainHistory1 = PortfolioGainHistory.create(
			Money.won(10000L),
			Money.won(10000L),
			Money.won(1000000L),
			Money.won(110000L),
			portfolio
		);

		PortfolioGainHistory portfolioGainHistory2 = PortfolioGainHistory.create(
			Money.won(20000L),
			Money.won(10000L),
			Money.won(1000000L),
			Money.won(120000L),
			portfolio
		);
		portfolioGainHistoryRepository.save(portfolioGainHistory1);
		portfolioGainHistoryRepository.save(portfolioGainHistory2);
		// when
		List<LineChartItem> actual = portfolioGainHistoryRepository.findDailyTotalAmountByPortfolioId(
			portfolio.getId());
		// then
		Assertions.assertThat(actual).hasSize(1);
	}

	@DisplayName("여러개의 포트폴리오의 포트폴리오 손익 내역 데이터를 삭제한다")
	@Test
	void deleteAllByPortfolioIds() {
		// given
		Member member = memberRepository.save(TestDataFactory.createMember());
		Portfolio portfolio = portfolioRepository.save(TestDataFactory.createPortfolio(member, "포트폴리오1"));
		Portfolio portfolio2 = portfolioRepository.save(TestDataFactory.createPortfolio(member, "포트폴리오2"));
		PortfolioGainHistory portfolioGainHistory1 = PortfolioGainHistory.create(
			Money.won(10000L),
			Money.won(10000L),
			Money.won(1000000L),
			Money.won(110000L),
			portfolio
		);
		PortfolioGainHistory portfolioGainHistory2 = PortfolioGainHistory.create(
			Money.won(10000L),
			Money.won(10000L),
			Money.won(1000000L),
			Money.won(110000L),
			portfolio2
		);
		portfolioGainHistoryRepository.saveAll(List.of(portfolioGainHistory1, portfolioGainHistory2));
		// when
		portfolioGainHistoryRepository.deleteAllByPortfolioIds(List.of(portfolio.getId(), portfolio2.getId()));
		// then
		List<PortfolioGainHistory> actual = portfolioGainHistoryRepository.findAll();
		Assertions.assertThat(actual).isEmpty();
		;
	}
}
