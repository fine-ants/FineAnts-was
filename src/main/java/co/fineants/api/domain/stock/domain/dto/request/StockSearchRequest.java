package co.fineants.api.domain.stock.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class StockSearchRequest {
	private String searchTerm;

	@JsonCreator
	public StockSearchRequest(@JsonProperty("searchTerm") String searchTerm) {
		this.searchTerm = searchTerm;
	}
}
