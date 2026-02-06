package co.fineants.stock.application.aop;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
import co.fineants.api.domain.stock_target_price.domain.entity.TargetPriceNotification;
import co.fineants.api.domain.stock_target_price.repository.StockTargetPriceRepository;
import co.fineants.api.domain.stock_target_price.repository.TargetPriceNotificationRepository;
import co.fineants.api.domain.watchlist.domain.entity.WatchList;
import co.fineants.api.domain.watchlist.repository.WatchListRepository;
import co.fineants.api.domain.watchlist.repository.WatchStockRepository;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.stock.annotation.ActiveStockMarker;
import co.fineants.stock.annotation.ResourceType;
import co.fineants.stock.domain.ActiveStockRepository;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockRepository;
import lombok.extern.slf4j.Slf4j;

class ActiveStockAspectTest extends AbstractContainerBaseTest {

	@Autowired
	private ActiveStockAspect aspect;

	@Mock
	private JoinPoint joinPoint;

	@Mock
	private MethodSignature methodSignature;

	@Autowired
	private ActiveStockRepository activeStockRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private PortfolioHoldingRepository portfolioHoldingRepository;

	@Autowired
	private StockTargetPriceRepository stockTargetPriceRepository;

	@Autowired
	private TargetPriceNotificationRepository targetPriceNotificationRepository;

	@Autowired
	private WatchListRepository watchListRepository;

	@Autowired
	private WatchStockRepository watchStockRepository;

	@BeforeEach
	void setUp() throws NoSuchMethodException {
		Stock stock = stockRepository.save(createSamsungStock());
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		StockTargetPrice stockTargetPrice = stockTargetPriceRepository.save(
			StockTargetPrice.newStockTargetPriceWithActive(member, stock));
		Money targetPrice = Money.won(60000L);
		targetPriceNotificationRepository.save(
			TargetPriceNotification.newTargetPriceNotification(targetPrice, stockTargetPrice));
		WatchList watchList = watchListRepository.save(TestDataFactory.createWatchList("My WatchList 1", member));
		watchStockRepository.save(TestDataFactory.createWatchStock(stock, watchList));

		MockitoAnnotations.openMocks(this);
		// JoinPoint 설정 : 메서드 인자와 파라미터 설정
		Method method = TestTarget.class.getMethod("sampleMethod", MemberAuthentication.class, Long.class,
			String.class, Long.class);
		BDDMockito.given(joinPoint.getSignature())
			.willReturn(methodSignature);
		BDDMockito.given(methodSignature.getMethod())
			.willReturn(method);
		MemberAuthentication memberAuthentication = MemberAuthentication.from(member, Set.of("ROLE_USER"));
		BDDMockito.given(joinPoint.getArgs())
			.willReturn(new Object[] {memberAuthentication, portfolio.getId(), stock.getTickerSymbol()});
	}

	@DisplayName("객체 생성")
	@Test
	void canCreated() {
		Assertions.assertThat(aspect).isNotNull();
	}

	@DisplayName("활성 종목 등록")
	@ParameterizedTest
	@MethodSource(value = {"co.fineants.TestDataProvider#validResourceIdAndTypes"})
	void markBeforeController_whenValidResourceId_thenRegistersActiveStock(String resourceId, ResourceType type) {
		// given
		ActiveStockMarker marker = createMarker(resourceId, type);

		// when
		aspect.markBeforeController(joinPoint, marker);

		// then
		Awaitility.await()
			.atMost(Duration.ofSeconds(5))
			.untilAsserted(() -> Assertions.assertThat(activeStockRepository.size()).isEqualTo(1L));
	}

	@DisplayName("활성 종목 등록 실패 - 잘못된 ResourceId를 전달하면 예외가 발생한다.")
	@ParameterizedTest
	@MethodSource(value = {"co.fineants.TestDataProvider#invalidResourceIds"})
	void markBeforeController_invalidResourceId_throwsException(String invalidResourceId) {
		// given
		ActiveStockMarker marker = createMarker(invalidResourceId, ResourceType.MEMBER);

		// when & then
		Assertions.assertThatThrownBy(() -> aspect.markBeforeController(joinPoint, marker))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("Failed to process ActiveStockMarker");
	}

	@DisplayName("활성 종목 등록 실패 - 리소스 타입과 매개변수 값이 일치하지 않으면 예외가 발생한다.")
	@ParameterizedTest
	@MethodSource(value = {"co.fineants.TestDataProvider#invalidArgsForMemberResourceType"})
	void markBeforeController_whenInvalidTypeCasting_throwsException(String resourceId, ResourceType type,
		Object[] args) {
		// given
		ActiveStockMarker marker = createMarker(resourceId, type);
		BDDMockito.given(joinPoint.getArgs())
			.willReturn(args);

		// when & then
		Assertions.assertThatThrownBy(() -> aspect.markBeforeController(joinPoint, marker))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("Failed to process ActiveStockMarker");
	}

	private ActiveStockMarker createMarker(String resourceId, ResourceType type) {
		return new ActiveStockMarker() {
			@Override
			public String resourceId() {
				return resourceId;
			}

			@Override
			public ResourceType type() {
				return type;
			}

			@Override
			public Class<? extends java.lang.annotation.Annotation> annotationType() {
				return ActiveStockMarker.class;
			}
		};
	}

	@Slf4j
	private static class TestTarget {
		public void sampleMethod(MemberAuthentication authentication, Long portfolioId, String tickerSymbol,
			Long watchlistId) {
			log.debug("sampleMethod called");
		}
	}
}
