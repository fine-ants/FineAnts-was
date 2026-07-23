package co.fineants.api.domain.gainhistory.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.gainhistory.domain.dto.response.PortfolioGainHistoryCreateResponse;
import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.gainhistory.repository.PortfolioGainHistoryRepository;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.member.domain.Member;

@ExtendWith(MockitoExtension.class)
class PortfolioGainHistoryServiceUnitTest {

	@Mock
	private PortfolioGainHistoryRepository portfolioGainHistoryRepository;

	@Mock
	private PortfolioRepository portfolioRepository;

	@Mock
	private PortfolioCalculator portfolioCalculator;

	@Mock
	private LocalDateTimeService timeService;

	@InjectMocks
	private PortfolioGainHistoryService service;

	@DisplayName("모든 포트폴리오의 손익 내역을 추가한다")
	@Test
	void addPortfolioGainHistory() {
		// given
		Member member = TestDataFactory.createMember();
		Portfolio savePortfolio = TestDataFactory.createPortfolio(member);

		BDDMockito.given(portfolioRepository.findAll())
			.willReturn(List.of(savePortfolio));
		LocalDateTime now = LocalDate.of(2026, 7, 20).atStartOfDay();
		BDDMockito.given(timeService.getLocalDateTimeWithNow())
			.willReturn(now);

		PortfolioGainHistory latestHistory = PortfolioGainHistory.empty(savePortfolio);
		BDDMockito.given(portfolioGainHistoryRepository.findFirstLatestPortfolioGainHistory(
				savePortfolio.getId(), now, PageRequest.of(0, 1)))
			.willReturn(List.of(latestHistory));

		Money totalGain = Money.won(30_000L);
		BDDMockito.given(portfolioCalculator.calTotalGainBy(savePortfolio))
			.willReturn(totalGain);
		Money dailyGain = Money.won(30_000L);
		BDDMockito.given(portfolioCalculator.calDailyGain(latestHistory, savePortfolio))
			.willReturn(dailyGain);
		Money balance = Money.won(850_000L);
		BDDMockito.given(portfolioCalculator.calBalanceBy(savePortfolio))
			.willReturn(balance);
		Money totalCurrentValuation = Money.won(180_000L);
		BDDMockito.given(portfolioCalculator.calTotalCurrentValuationBy(savePortfolio))
			.willReturn(totalCurrentValuation);

		PortfolioGainHistory newHistory = PortfolioGainHistory.create(
			totalGain,
			dailyGain,
			balance,
			totalCurrentValuation,
			savePortfolio);
		PortfolioGainHistory savedHistory = PortfolioGainHistory.create(
			1L,
			totalGain,
			dailyGain,
			balance,
			totalCurrentValuation,
			savePortfolio);
		BDDMockito.given(portfolioGainHistoryRepository.save(newHistory))
			.willReturn(savedHistory);

		// when
		PortfolioGainHistoryCreateResponse response = service.addPortfolioGainHistory();

		// then
		assertThat(response.getIds())
			.asList()
			.hasSize(1)
			.containsExactly(1L);
	}
}
