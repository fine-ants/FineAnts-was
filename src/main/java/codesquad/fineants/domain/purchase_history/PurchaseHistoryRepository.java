package codesquad.fineants.domain.purchase_history;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseHistoryRepository extends JpaRepository<PurchaseHistory, Long> {

	@Query("select p from PurchaseHistory p where p.portfolioHolding.id in(:holdingIds)")
	List<PurchaseHistory> findAllByHoldingIds(@Param("holdingIds") List<Long> holdingIds);

	List<PurchaseHistory> findAllByPortfolioHoldingId(Long portfolioStockId);

	int deleteAllByPortfolioHoldingIdIn(List<Long> portfolioId);

	void deleteByPortfolioHoldingId(Long portfolioHoldingId);

}
