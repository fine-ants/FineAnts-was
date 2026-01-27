package co.fineants.api.domain.kis.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.api.domain.common.money.Money;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * &#064;EqualsAndHashCode 에서 lastUpdatedAt 필드를 제외한 이유는 다음과 같습니다.
 * lastUpdatedAt 필드는 데이터의 시간적 상태를 나타내는 가변적인 값으로, 객체의 고유성을 판단하는 데 적합하지 않다고 판단하여 제외함.
 */
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
	private CurrentPriceRedisEntity(
		@JsonProperty("tickerSymbol") String tickerSymbol,
		@JsonProperty("price") long price,
		@JsonProperty("lastUpdatedAt") long lastUpdatedAt) {
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
	@JsonIgnore
	public boolean isFresh(long currentTimeMillis, long thresholdMillis) {
		return currentTimeMillis - lastUpdatedAt <= thresholdMillis;
	}

	@JsonIgnore
	public Money getPriceMoney() {
		return Money.won(price);
	}
}
