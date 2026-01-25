package co.fineants.api.domain.kis.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.kis.repository.PriceRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurrentPriceService {
	private final PriceRepository priceRepository;

	public Optional<Money> fetchPrice(String tickerSymbol) {
		return priceRepository.fetchPriceBy(tickerSymbol);
	}
}
