package co.fineants.api.domain.kis.repository;

import java.util.Optional;
import java.util.Set;

import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.CurrentPriceRedisEntity;
import co.fineants.stock.domain.Stock;

public interface CurrentPriceRepository {
	void savePrice(KisCurrentPrice... currentPrices);

	void savePrice(Stock stock, long price);

	void savePrice(String tickerSymbol, long price);

	Optional<CurrentPriceRedisEntity> fetchPriceBy(String tickerSymbol);

	Set<CurrentPriceRedisEntity> findAll();

	void clear();
}
