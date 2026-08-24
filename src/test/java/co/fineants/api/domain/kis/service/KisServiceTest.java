package co.fineants.api.domain.kis.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.kis.client.KisAccessToken;
import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.CurrentPriceRedisEntity;
import co.fineants.api.domain.kis.domain.dto.response.KisDividend;
import co.fineants.api.domain.kis.domain.dto.response.KisDividendWrapper;
import co.fineants.api.domain.kis.repository.CurrentPriceRepository;
import co.fineants.api.domain.kis.repository.infrastructure.KisAccessTokenInMemoryRepository;
import co.fineants.api.domain.notification.event.publisher.PortfolioPublisher;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.stock_target_price.event.publisher.StockTargetPricePublisher;
import co.fineants.api.global.common.delay.DelayManager;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
@Transactional
class KisServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private KisService kisService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private PortfolioHoldingRepository portfolioHoldingRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private KisAccessTokenInMemoryRepository kisAccessTokenInMemoryRepository;

	@Autowired
	private KisAccessTokenService kisAccessTokenService;

	@Autowired
	private CurrentPriceRepository currentPriceRepository;

	@Autowired
	private CurrentPriceService currentPriceService;

	@Autowired
	private ClosingPriceService closingPriceService;

	@Autowired
	private StockTargetPricePublisher stockTargetPricePublisher;

	@Autowired
	private PortfolioPublisher portfolioPublisher;

	@Autowired
	private LocalDateTimeService spyLocalDateTimeService;

	@Autowired
	private KisClient mockedKisClient;

	@Autowired
	private DelayManager spyDelayManager;

	@BeforeEach
	void setUp() {
		kisService = new KisService(
			mockedKisClient,
			currentPriceService,
			closingPriceService,
			stockTargetPricePublisher,
			portfolioPublisher,
			spyDelayManager,
			kisAccessTokenService,
			stockRepository,
			spyLocalDateTimeService
		);
	}

	@AfterEach
	void tearDown() {
		Mockito.clearInvocations(mockedKisClient);
		kisAccessTokenInMemoryRepository.save(null);
		kisAccessTokenService.deleteAccessTokenMap();
	}

	@DisplayName("현재가를 갱신할때 액세스 토큰의 만료시간이 1시간 이전어서 새로운 액세스 토큰을 재발급한다")
	@Test
	void refreshStockCurrentPrice_whenAccessTokenSoonExpired_thenFetchAccessToken() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		List<Stock> stocks = stockRepository.saveAll(List.of(createSamsungStock()));
		stocks.forEach(stock -> portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock)));

		given(mockedKisClient.fetchCurrentPrice("005930"))
			.willReturn(Mono.just(KisCurrentPrice.create("005930", 10000L)));
		given(spyDelayManager.delay()).willReturn(Duration.ZERO);
		given(spyDelayManager.fixedDelay()).willReturn(Duration.ZERO);

		List<String> tickerSymbols = stocks.stream()
			.map(Stock::getTickerSymbol)
			.toList();

		KisAccessToken soonExpiredAccessToken = KisAccessToken.bearerType("accessToken",
			LocalDateTime.now().plusMinutes(10), 6000);
		kisAccessTokenInMemoryRepository.save(soonExpiredAccessToken);
		kisAccessTokenService.saveAccessToken(soonExpiredAccessToken, LocalDateTime.now());

		KisAccessToken reloadAccessToken = createKisAccessToken();
		given(mockedKisClient.fetchAccessToken())
			.willReturn(Mono.just(reloadAccessToken));

		// when
		kisService.refreshStockCurrentPrice(tickerSymbols);

		// then
		assertThat(kisAccessTokenService.getAuthorization()).isEqualTo(reloadAccessToken.createAuthorization());
		assertThat(kisAccessTokenService.getAccessToken().orElseThrow().getAccessToken()).isEqualTo(
			reloadAccessToken.getAccessToken());
		CurrentPriceRedisEntity actual = currentPriceRepository.fetchPriceBy("005930").orElseThrow();
		assertThat(actual)
			.hasFieldOrPropertyWithValue("tickerSymbol", "005930")
			.hasFieldOrPropertyWithValue("price", 10000L);
	}

	@DisplayName("사용자는 새로운 한국투자증권의 액세스 토큰을 발급받아서 배당 일정을 조회한다")
	@Test
	void fetchDividend_whenAccessTokenExpired_thenIssueAccessToken() {
		// given
		String tickerSymbol = "005930";
		kisAccessTokenInMemoryRepository.save(null);
		KisAccessToken newKisAccessToken = createKisAccessToken();
		given(mockedKisClient.fetchAccessToken())
			.willReturn(Mono.just(newKisAccessToken));
		given(mockedKisClient.fetchDividendThisYear(tickerSymbol))
			.willReturn(Mono.just(KisDividendWrapper.create(List.of(
				KisDividend.create(tickerSymbol, Money.won(300), LocalDate.of(2024, 3, 1),
					LocalDate.of(2024, 5, 1))))));
		// when
		Flux<KisDividend> dividends = kisService.fetchDividend(tickerSymbol);
		// then
		StepVerifier.create(dividends)
			.expectNext(
				KisDividend.create("005930", Money.won(300), LocalDate.of(2024, 3, 1), LocalDate.of(2024, 5, 1)))
			.expectComplete()
			.verify();
	}
}
