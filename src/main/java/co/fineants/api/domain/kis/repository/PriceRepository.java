package co.fineants.api.domain.kis.repository;

import java.util.Optional;

import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.CurrentPriceRedisEntity;
import co.fineants.stock.domain.Stock;

public interface PriceRepository {
	void savePrice(KisCurrentPrice... currentPrices);

	void savePrice(Stock stock, long price);

	void savePrice(String tickerSymbol, long price);

	Optional<CurrentPriceRedisEntity> fetchPriceBy(String tickerSymbol);

	void clear();
}
