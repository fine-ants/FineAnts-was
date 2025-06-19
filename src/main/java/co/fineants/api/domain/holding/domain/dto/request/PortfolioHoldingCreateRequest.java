package co.fineants.api.domain.holding.domain.dto.request;

import java.util.Optional;

import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.purchasehistory.domain.dto.request.PurchaseHistoryCreateRequest;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class PortfolioHoldingCreateRequest {
	@NotBlank(message = "티커심볼은 필수 정보입니다")
	private String tickerSymbol;
	private PurchaseHistoryCreateRequest purchaseHistory;

	public static PortfolioHoldingCreateRequest create(String tickerSymbol,
		PurchaseHistoryCreateRequest purchaseHistory) {
		return new PortfolioHoldingCreateRequest(tickerSymbol, purchaseHistory);
	}

	public Optional<PurchaseHistory> toPurchaseHistoryEntity(PortfolioHolding saveHolding) {
		if (purchaseHistory == null) {
			return Optional.empty();
		}
		return purchaseHistory.toEntity(saveHolding);
	}
}
