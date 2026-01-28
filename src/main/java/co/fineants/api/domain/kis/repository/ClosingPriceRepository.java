package co.fineants.api.domain.kis.repository;

import java.util.Optional;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.kis.domain.dto.response.KisClosingPrice;

public interface ClosingPriceRepository {
	void addPrice(String tickerSymbol, long price);

	void addPrice(KisClosingPrice price);

	Optional<Money> fetchPrice(String tickerSymbol);
}
