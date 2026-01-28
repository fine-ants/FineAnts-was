package co.fineants.api.domain.kis.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.api.domain.common.money.Money;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = {"tickerSymbol", "price"})
public class ClosingPriceRedisEntity {
	@JsonProperty("tickerSymbol")
	private final String tickerSymbol;
	@JsonProperty("price")
	private final long price;
	@JsonProperty("lastUpdatedAt")
	private final long lastUpdatedAt;

	@JsonCreator
	private ClosingPriceRedisEntity(
		@JsonProperty("tickerSymbol") String tickerSymbol,
		@JsonProperty("price") long price,
		@JsonProperty("lastUpdatedAt") long lastUpdatedAt) {
		this.tickerSymbol = tickerSymbol;
		this.price = price;
		this.lastUpdatedAt = lastUpdatedAt;
	}

	public static ClosingPriceRedisEntity of(String tickerSymbol, long price, long lastUpdatedAt) {
		return new ClosingPriceRedisEntity(tickerSymbol, price, lastUpdatedAt);
	}

	@JsonIgnore
	public Money getPriceMoney() {
		return Money.won(price);
	}

	@Override
	public String toString() {
		return "종목 종가 레디스 엔티티(tickerSymbol=" + tickerSymbol + ", price=" + price + "원)";
	}
}
