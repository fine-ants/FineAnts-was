package codesquad.fineants.domain.target_price_notification;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.stock_target_price.StockTargetPrice;

public interface TargetPriceNotificationRepository extends JpaRepository<TargetPriceNotification, Long> {

	@Query("select t from TargetPriceNotification t where t.stockTargetPrice.id = :stockTargetPriceId")
	List<TargetPriceNotification> findAllByStockTargetPriceId(@Param("stockTargetPriceId") Long stockTargetPriceId);

	@Query("select t from TargetPriceNotification t where t.stockTargetPrice.stock.tickerSymbol = :tickerSymbol and t.targetPrice = :targetPrice and t.stockTargetPrice.member.id = :memberId")
	Optional<TargetPriceNotification> findByTickerSymbolAndTargetPriceAndMemberId(
		@Param("tickerSymbol") String tickerSymbol,
		@Param("targetPrice") Money targetPrice,
		@Param("memberId") Long memberId);

	@Modifying
	@Query("delete from TargetPriceNotification t where t.id in (:targetPriceNotificationIds)")
	int deleteAllByTargetPriceNotificationIds(
		@Param("targetPriceNotificationIds") List<Long> targetPriceNotificationIds);

	@Modifying
	@Query("delete from TargetPriceNotification t where t.stockTargetPrice in(:stockTargetPrices)")
	int deleteAllByStockTargetPrices(@Param("stockTargetPrices") List<StockTargetPrice> stockTargetPrices);
}
