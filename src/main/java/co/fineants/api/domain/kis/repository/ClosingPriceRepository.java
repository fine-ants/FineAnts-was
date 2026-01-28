package co.fineants.api.domain.kis.repository;

import java.util.Optional;

import co.fineants.api.domain.kis.domain.ClosingPriceRedisEntity;
import co.fineants.api.domain.kis.domain.dto.response.KisClosingPrice;

public interface ClosingPriceRepository {
	void savePrice(String tickerSymbol, long price);

	void savePrice(KisClosingPrice price);

	Optional<ClosingPriceRedisEntity> fetchPrice(String tickerSymbol);
}
