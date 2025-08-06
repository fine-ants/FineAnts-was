package co.fineants.api.domain.gainhistory.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.portfolio.domain.dto.response.LineChartItem;

public interface PortfolioGainHistoryRepository extends JpaRepository<PortfolioGainHistory, Long> {

	@Query(value = """
		select p, p2 from PortfolioGainHistory p
		inner join Portfolio p2 on p.portfolio.id = p2.id
		where p.portfolio.id = :portfolioId and p.createAt <= :createAt
		order by p.createAt desc
		""")
	List<PortfolioGainHistory> findFirstLatestPortfolioGainHistory(
		@Param("portfolioId") Long portfolioId, @Param("createAt") LocalDateTime createAt, Pageable pageable);

	@Query(value = """
		select date(p.create_at) as date, sum(p.cash + p.current_valuation) as totalValuation
		from fineAnts.portfolio_gain_history p
		where p.portfolio_id = :portfolioId
		group by date(p.create_at)
		order by date(p.create_at) desc
		""", nativeQuery = true)
	List<LineChartItem> findDailyTotalAmountByPortfolioId(@Param("portfolioId") Long portfolioId);

	@Modifying
	@Query("delete from PortfolioGainHistory p where p.portfolio.id = :portfolioId")
	int deleteAllByPortfolioId(@Param("portfolioId") Long portfolioId);

	@Modifying
	@Query("delete from PortfolioGainHistory p where p.portfolio.id in (:portfolioIds)")
	void deleteAllByPortfolioIds(@Param("portfolioIds") List<Long> portfolioIds);
}
