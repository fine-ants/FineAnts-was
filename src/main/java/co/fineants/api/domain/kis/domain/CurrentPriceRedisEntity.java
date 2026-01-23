package co.fineants.api.domain.kis.domain;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrentPriceRedisEntity {
	private final String tickerSymbol;
	private final long price;
	private final long lastUpdatedAt;

	public static CurrentPriceRedisEntity now(String tickerSymbol, long price) {
		return new CurrentPriceRedisEntity(tickerSymbol, price, System.currentTimeMillis());
	}

	/**
	 * 신선도를 판단합니다.
	 * 신선도: 마지막 업데이트 시점이 현재 시점으로부터 thresholdMillis 이내인 경우
	 * 예를 들어 thresholdMillis가 5000이라면, 마지막 업데이트가 5초 이내인 경우 신선하다고 판단합니다.
	 * @param thresholdMillis 시간 기준으로 신선한지 여부
	 * @return 신선하면 true, 아니면 false
	 */
	public boolean isFresh(long thresholdMillis) {
		return System.currentTimeMillis() - lastUpdatedAt <= thresholdMillis;
	}
}
