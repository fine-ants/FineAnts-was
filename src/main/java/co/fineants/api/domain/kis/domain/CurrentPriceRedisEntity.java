package co.fineants.api.domain.kis.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = {"tickerSymbol", "price"})
public class CurrentPriceRedisEntity {
	@JsonProperty("tickerSymbol")
	private final String tickerSymbol;
	@JsonProperty("price")
	private final long price;
	@JsonProperty("lastUpdatedAt")
	private final long lastUpdatedAt;

	@JsonCreator
	private CurrentPriceRedisEntity(@JsonProperty("tickerSymbol") String tickerSymbol,
		@JsonProperty("price") long price, @JsonProperty("lastUpdatedAt") long lastUpdatedAt) {
		this.tickerSymbol = tickerSymbol;
		this.price = price;
		this.lastUpdatedAt = lastUpdatedAt;
	}

	public static CurrentPriceRedisEntity of(String tickerSymbol, long price, long lastUpdatedAt) {
		return new CurrentPriceRedisEntity(tickerSymbol, price, lastUpdatedAt);
	}

	/**
	 * 신선도를 판단합니다.
	 * 신선도: 마지막 업데이트 시점이 현재 시점으로부터 thresholdMillis 이내인 경우
	 * 예를 들어 thresholdMillis가 5000이라면, 마지막 업데이트가 5초 이내인 경우 신선하다고 판단합니다.
	 *
	 * @param currentTimeMillis 현재 시간 밀리초
	 * @param thresholdMillis   시간 기준으로 신선한지 여부
	 * @return 신선하면 true, 아니면 false
	 */
	public boolean isFresh(long currentTimeMillis, long thresholdMillis) {
		return currentTimeMillis - lastUpdatedAt <= thresholdMillis;
	}
}
