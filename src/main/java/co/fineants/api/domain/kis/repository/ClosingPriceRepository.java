package co.fineants.api.domain.kis.repository;

import java.util.Optional;

import co.fineants.api.domain.kis.domain.ClosingPriceRedisEntity;
import co.fineants.api.domain.kis.domain.dto.response.KisClosingPrice;

public interface ClosingPriceRepository {
	void savePrice(KisClosingPrice price);

	void savePrice(String tickerSymbol, long price);

	Optional<ClosingPriceRedisEntity> fetchPrice(String tickerSymbol);
}
