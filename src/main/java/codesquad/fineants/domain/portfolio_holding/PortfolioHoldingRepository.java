package codesquad.fineants.domain.portfolio_holding;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import codesquad.fineants.domain.portfolio.Portfolio;

public interface PortfolioHoldingRepository extends JpaRepository<PortfolioHolding, Long> {

	List<PortfolioHolding> findAllByPortfolio(Portfolio portfolio);

	@Query("select distinct s.tickerSymbol from PortfolioHolding p inner join Stock s on p.stock.tickerSymbol = s.tickerSymbol")
	List<String> findAllTickerSymbol();

	@Query("SELECT p FROM PortfolioHolding p WHERE p.stock.tickerSymbol = :tickerSymbol")
	Optional<PortfolioHolding> findByTickerSymbol(@Param("tickerSymbol") String tickerSymbol);

	@Query("select p from PortfolioHolding p where p.portfolio.id = :portfolioId and p.stock.tickerSymbol = :tickerSymbol")
	Optional<PortfolioHolding> findByPortfolioIdAndTickerSymbol(
		@Param("portfolioId") Long portfolioId,
		@Param("tickerSymbol") String tickerSymbol);

	@Query("select p from PortfolioHolding p join fetch p.portfolio where p.id = :portfolioHoldingId and p.portfolio.id = :portfolioId")
	Optional<PortfolioHolding> findByPortfolioHoldingIdAndPortfolioIdWithPortfolio(
		@Param("portfolioHoldingId") Long portfolioHoldingId,
		@Param("portfolioId") Long portfolioId);

	@Query("select count(p) > 0 from PortfolioHolding p where p.id = :portfolioHoldingId and p.portfolio.member.id = :memberId")
	boolean existsByIdAndMemberId(
		@Param("portfolioHoldingId") Long portfolioHoldingId,
		@Param("memberId") Long memberId);

	int deleteAllByPortfolioId(Long portFolioId);

	int deleteAllByIdIn(List<Long> portfolioHoldingIds);
}
