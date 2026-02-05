package co.fineants.api.domain.kis.service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import co.fineants.api.domain.kis.client.KisAccessToken;
import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.dto.response.KisClosingPrice;
import co.fineants.api.domain.kis.domain.dto.response.KisDividend;
import co.fineants.api.domain.kis.domain.dto.response.KisDividendWrapper;
import co.fineants.api.domain.kis.domain.dto.response.KisIpo;
import co.fineants.api.domain.kis.domain.dto.response.KisIpoResponse;
import co.fineants.api.domain.kis.domain.dto.response.KisSearchStockInfo;
import co.fineants.api.domain.kis.repository.KisAccessTokenRepository;
import co.fineants.api.domain.notification.event.publisher.PortfolioPublisher;
import co.fineants.api.domain.stock_target_price.event.publisher.StockTargetPricePublisher;
import co.fineants.api.global.common.delay.DelayManager;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.global.errors.exception.business.CredentialsTypeKisException;
import co.fineants.api.global.errors.exception.business.ExpiredAccessTokenKisException;
import co.fineants.api.global.errors.exception.business.RequestLimitExceededKisException;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockRepository;
import co.fineants.stock.presentation.dto.response.StockDataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@RequiredArgsConstructor
@Service
public class KisService {
	private static final int MAX_ATTEMPTS = 5;
	private static final int CONCURRENCY = 20;

	private final KisClient kisClient;
	private final CurrentPriceService currentPriceService;
	private final ClosingPriceService closingPriceService;
	private final StockTargetPricePublisher stockTargetPricePublisher;
	private final PortfolioPublisher portfolioPublisher;
	private final DelayManager delayManager;
	private final KisAccessTokenRepository kisAccessTokenRepository;
	private final KisAccessTokenRedisService kisAccessTokenRedisService;
	private final StockRepository stockRepository;
	private final LocalDateTimeService localDateTimeService;

	// 주식 현재가 갱신
	public List<KisCurrentPrice> refreshStockCurrentPrice(Collection<String> tickerSymbols) {
		List<KisCurrentPrice> prices = Flux.fromIterable(tickerSymbols)
			.flatMap(this::fetchCurrentPrice, CONCURRENCY)
			.delayElements(delayManager.delay())
			.collectList()
			.blockOptional(delayManager.timeout())
			.orElseGet(Collections::emptyList);
		prices.forEach(price ->
			currentPriceService.savePrice(price.getTickerSymbol(), price.getPrice())
		);
		stockTargetPricePublisher.publishEvent(tickerSymbols);
		portfolioPublisher.publishCurrentPriceEvent();
		return prices;
	}

	public Mono<KisCurrentPrice> fetchCurrentPrice(String tickerSymbol) {
		return Mono.defer(() -> kisClient.fetchCurrentPrice(tickerSymbol))
			.map(kisCurrentPrice -> {
				if (kisCurrentPrice.getTickerSymbol() == null) {
					return KisCurrentPrice.empty(tickerSymbol);
				}
				return kisCurrentPrice;
			})
			.doOnSuccess(kisCurrentPrice -> log.debug("reload stock current price {}", kisCurrentPrice))
			.onErrorResume(ExpiredAccessTokenKisException.class::isInstance, throwable -> Mono.empty())
			.onErrorResume(CredentialsTypeKisException.class::isInstance, throwable -> Mono.empty())
			.retryWhen(Retry.fixedDelay(MAX_ATTEMPTS, delayManager.fixedDelay())
				.filter(RequestLimitExceededKisException.class::isInstance))
			.onErrorResume(Exceptions::isRetryExhausted, throwable -> Mono.empty())
			.timeout(delayManager.timeout());
	}

	public List<KisClosingPrice> refreshAllClosingPrice() {
		return refreshClosingPrice(stockRepository.findAll().stream()
			.map(Stock::getTickerSymbol)
			.toList());
	}

	public List<KisClosingPrice> refreshClosingPrice(List<String> tickerSymbols) {
		List<KisClosingPrice> prices = Flux.fromIterable(tickerSymbols)
			.flatMap(ticker -> this.fetchClosingPrice(ticker)
				.doOnSuccess(price -> log.debug("reload stock closing price {}", price))
				.onErrorResume(ExpiredAccessTokenKisException.class::isInstance, throwable -> Mono.empty())
				.onErrorResume(CredentialsTypeKisException.class::isInstance, throwable -> Mono.empty())
				.retryWhen(Retry.fixedDelay(MAX_ATTEMPTS, delayManager.fixedDelay())
					.filter(RequestLimitExceededKisException.class::isInstance))
				.onErrorResume(Exceptions::isRetryExhausted, throwable -> Mono.empty()), CONCURRENCY)
			.delayElements(delayManager.delay())
			.collectList()
			.blockOptional(delayManager.timeout())
			.orElseGet(Collections::emptyList);

		prices.forEach(price ->
			closingPriceService.savePrice(price.getTickerSymbol(), price.getPrice())
		);
		log.info("종목 종가 {}개중 {}개 갱신", tickerSymbols.size(), prices.size());
		return prices;
	}

	public Mono<KisClosingPrice> fetchClosingPrice(String tickerSymbol) {
		return Mono.defer(() -> kisClient.fetchClosingPrice(tickerSymbol))
			.map(kisClosingPrice -> {
				if (kisClosingPrice.getTickerSymbol() == null) {
					return KisClosingPrice.empty(tickerSymbol);
				}
				return kisClosingPrice;
			})
			.doOnSuccess(kisClosingPrice -> log.debug("reload stock closing price {}", kisClosingPrice))
			.onErrorResume(ExpiredAccessTokenKisException.class::isInstance, throwable -> Mono.empty())
			.onErrorResume(CredentialsTypeKisException.class::isInstance, throwable -> Mono.empty())
			.retryWhen(Retry.fixedDelay(MAX_ATTEMPTS, delayManager.fixedDelay())
				.filter(RequestLimitExceededKisException.class::isInstance))
			.onErrorResume(Exceptions::isRetryExhausted, throwable -> Mono.empty())
			.timeout(delayManager.timeout());
	}

	/**
	 * tickerSymbol에 해당하는 종목의 배당 일정을 조회합니다.
	 *
	 * @param tickerSymbol 종목 단축 코드
	 * @return 종목의 배당 일정 정보
	 */
	public Flux<KisDividend> fetchDividend(String tickerSymbol) {
		return kisClient.fetchDividendThisYear(tickerSymbol)
			.map(KisDividendWrapper::getKisDividends)
			.doOnSuccess(response -> log.debug("fetchDividend response size is {}", response.size()))
			.onErrorResume(ExpiredAccessTokenKisException.class::isInstance, throwable -> Mono.empty())
			.onErrorResume(CredentialsTypeKisException.class::isInstance, throwable -> Mono.empty())
			.retryWhen(Retry.fixedDelay(MAX_ATTEMPTS, delayManager.fixedDelay())
				.filter(RequestLimitExceededKisException.class::isInstance))
			.onErrorResume(Exceptions::isRetryExhausted, throwable -> Mono.empty())
			.onErrorResume(throwable -> Mono.empty())
			.flatMapMany(Flux::fromIterable);
	}

	public List<KisDividend> fetchDividendsBetween(LocalDate from, LocalDate to) {
		return kisClient.fetchDividendsBetween(from, to)
			.doOnSuccess(dividends -> log.debug("dividends is {}", dividends))
			.onErrorResume(ExpiredAccessTokenKisException.class::isInstance, throwable -> Mono.empty())
			.onErrorResume(CredentialsTypeKisException.class::isInstance, throwable -> Mono.empty())
			.retryWhen(Retry.fixedDelay(MAX_ATTEMPTS, delayManager.fixedDelay())
				.filter(RequestLimitExceededKisException.class::isInstance))
			.onErrorResume(Exceptions::isRetryExhausted, throwable -> Mono.empty())
			.blockOptional(delayManager.timeout())
			.orElseGet(Collections::emptyList).stream()
			.sorted()
			.toList();
	}

	/**
	 * 종목 기본 조회
	 *
	 * @param tickerSymbol 종목 티커 심볼
	 * @return 종목 정보
	 */
	public Mono<KisSearchStockInfo> fetchSearchStockInfo(String tickerSymbol) {
		return kisClient.fetchSearchStockInfo(tickerSymbol)
			.doOnSuccess(response -> log.debug("fetchSearchStockInfo ticker is {}", response))
			.onErrorResume(ExpiredAccessTokenKisException.class::isInstance, throwable -> Mono.empty())
			.onErrorResume(CredentialsTypeKisException.class::isInstance, throwable -> Mono.empty())
			.retryWhen(Retry.fixedDelay(MAX_ATTEMPTS, delayManager.fixedDelay())
				.filter(RequestLimitExceededKisException.class::isInstance))
			.onErrorResume(Exceptions::isRetryExhausted, throwable -> Mono.empty())
			.onErrorResume(throwable -> {
				log.error("fetchSearchStockInfo error message is {}", throwable.getMessage());
				return Mono.empty();
			});
	}

	/**
	 * 상장된 종목 조회
	 * 하루전부터 오늘까지의 상장된 종목들의 정보를 조회한다.
	 *
	 * @return 종목 정보 리스트
	 */
	public Flux<StockDataResponse.StockIntegrationInfo> fetchStockInfoInRangedIpo() {
		LocalDate today = localDateTimeService.getLocalDateWithNow();
		LocalDate yesterday = today.minusDays(1);
		Flux<String> tickerSymbols = kisClient.fetchIpo(yesterday, today)
			.onErrorResume(throwable -> {
				log.error("fetchIpo error message is {}", throwable.getMessage());
				return Mono.empty();
			})
			.map(KisIpoResponse::getKisIpos)
			.defaultIfEmpty(Collections.emptyList())
			.flatMapMany(Flux::fromIterable)
			.filter(kisIpo -> !kisIpo.isEmpty())
			.map(KisIpo::getShtCd);

		return tickerSymbols
			.flatMap(this::fetchSearchStockInfo, CONCURRENCY)
			.delayElements(delayManager.delay())
			.onErrorResume(throwable -> {
				log.error("fetchSearchStockInfo error message is {}", throwable.getMessage());
				return Mono.empty();
			})
			.map(KisSearchStockInfo::toEntity)
			.map(StockDataResponse.StockIntegrationInfo::from);
	}

	public KisAccessToken deleteAccessToken() {
		kisAccessTokenRepository.refreshAccessToken(null);
		KisAccessToken kisAccessToken = kisAccessTokenRedisService.getAccessTokenMap().orElse(null);
		kisAccessTokenRedisService.deleteAccessTokenMap();
		return kisAccessToken;
	}
}
