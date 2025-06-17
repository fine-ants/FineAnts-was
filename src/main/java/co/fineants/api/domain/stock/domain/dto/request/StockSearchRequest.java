package co.fineants.api.domain.stock.domain.dto.request;

import lombok.Getter;

@Getter
public class StockSearchRequest {
	private String searchTerm;

	public StockSearchRequest(String searchTerm) {
		this.searchTerm = searchTerm;
	}
}
