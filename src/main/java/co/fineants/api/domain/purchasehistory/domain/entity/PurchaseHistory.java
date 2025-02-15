package co.fineants.api.domain.purchasehistory.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import co.fineants.api.domain.BaseEntity;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.count.CountConverter;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.MoneyConverter;
import co.fineants.api.domain.dividend.domain.entity.DividendDates;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.purchasehistory.domain.dto.request.PurchaseHistoryCreateRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString(exclude = {"portfolioHolding"})
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PurchaseHistory extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDateTime purchaseDate;

	@Convert(converter = MoneyConverter.class)
	@Column(precision = 19, nullable = false)
	private Money purchasePricePerShare;

	@Convert(converter = CountConverter.class)
	private Count numShares;

	private String memo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "portfolio_holding_id")
	private PortfolioHolding portfolioHolding;

	public static PurchaseHistory of(PortfolioHolding portFolioHolding, PurchaseHistoryCreateRequest request) {
		return new PurchaseHistory(
			null,
			request.getPurchaseDate(),
			request.getPurchasePricePerShare(),
			request.getNumShares(),
			request.getMemo(),
			portFolioHolding
		);
	}

	public static PurchaseHistory now(Money purchasePricePerShare, Count numShares, String memo,
		PortfolioHolding holding) {
		return new PurchaseHistory(
			null,
			LocalDateTime.now(),
			purchasePricePerShare,
			numShares,
			memo,
			holding
		);
	}

	public static PurchaseHistory create(LocalDateTime purchaseDate, Count numShares, Money purchasePricePerShare,
		String memo, PortfolioHolding portfolioHolding) {
		return create(null, purchaseDate, numShares, purchasePricePerShare, memo, portfolioHolding);
	}

	public static PurchaseHistory create(Long id, LocalDateTime purchaseDate, Count numShares,
		Money purchasePricePerShare,
		String memo, PortfolioHolding portfolioHolding) {
		return new PurchaseHistory(id, purchaseDate, purchasePricePerShare, numShares, memo, portfolioHolding);
	}

	//== 연관 관계 메서드 ==//
	public void setHolding(PortfolioHolding holding) {
		this.portfolioHolding = holding;
	}

	/**
	 * 매입 이력 투자 금액 계산 후 반환.
	 * <p>
	 * InvestmentAmount = PurchasePricePerShare * NumShares
	 * </p>
	 * @return 매입 이력 투자 금액
	 */
	public Expression calInvestmentAmount() {
		return purchasePricePerShare.times(numShares.intValue());
	}

	public PurchaseHistory change(PurchaseHistory history) {
		this.purchaseDate = history.getPurchaseDate();
		this.purchasePricePerShare = history.getPurchasePricePerShare();
		this.numShares = history.getNumShares();
		this.memo = history.getMemo();
		return this;
	}

	public LocalDate getPurchaseLocalDate() {
		return purchaseDate.toLocalDate();
	}

	// 배당금을 받을 수 있는지 검사
	public boolean isSatisfiedDividend(LocalDate exDividendDate) {
		LocalDate purcahseLocalDate = purchaseDate.toLocalDate();
		return purcahseLocalDate.equals(exDividendDate)
			|| purcahseLocalDate.isBefore(exDividendDate);
	}

	public boolean hasAuthorization(Long memberId) {
		return this.portfolioHolding.hasAuthorization(memberId);
	}

	public boolean isPurchaseDateBeforeExDividendDate(LocalDateTime localDateTime) {
		return this.purchaseDate.isBefore(localDateTime);
	}

	public boolean canReceiveDividendOn(DividendDates dividendDates) {
		return dividendDates.canReceiveDividendOn(purchaseDate.toLocalDate());
	}
}
