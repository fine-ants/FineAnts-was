package co.fineants.api.domain.kis.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.CurrentPriceRedisEntity;
import co.fineants.api.domain.kis.repository.PriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrentPriceService {
	private final PriceRepository priceRepository;
	private final KisService kisService;

	public Money fetchPrice(String tickerSymbol) {
		Optional<CurrentPriceRedisEntity> entity = priceRepository.fetchPriceBy(tickerSymbol);

		if (entity.isEmpty()) {
			Optional<Long> freshPrice = fetchPriceFromKis(tickerSymbol);
			if (freshPrice.isPresent()) {
				priceRepository.savePrice(tickerSymbol, freshPrice.get());
				return Money.won(freshPrice.get());
			}
			throw new IllegalStateException("현재가를 가져올 수 없습니다. tickerSymbol=" + tickerSymbol);
		}
		return entity.get().getPriceMoney();
	}

	private Optional<Long> fetchPriceFromKis(String tickerSymbol) {
		return kisService.fetchCurrentPrice(tickerSymbol)
			.map(KisCurrentPrice::getPrice)
			.blockOptional();
	}
}
